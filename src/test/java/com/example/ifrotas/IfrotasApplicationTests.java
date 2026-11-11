package com.example.ifrotas;

import com.example.ifrotas.domain.Conexao;
import com.example.ifrotas.domain.PublicacaoRota;
import com.example.ifrotas.domain.Usuario;
import com.example.ifrotas.domain.Veiculo;
import com.example.ifrotas.dto.auth.CadastroRequestDTO;
import com.example.ifrotas.dto.rota.PublicacaoRotaRequestDTO;
import com.example.ifrotas.dto.veiculo.VeiculoRequestDTO;
import com.example.ifrotas.exception.EstadoInvalidoException;
import com.example.ifrotas.exception.RegraNegocioException;
import com.example.ifrotas.service.ConexaoService;
import com.example.ifrotas.service.MensagemChatService;
import com.example.ifrotas.service.PublicacaoRotaService;
import com.example.ifrotas.service.UsuarioService;
import com.example.ifrotas.service.VeiculoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class IfrotasApplicationTests {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private PublicacaoRotaService publicacaoRotaService;

    @Autowired
    private ConexaoService conexaoService;

    @Autowired
    private MensagemChatService mensagemChatService;

    @Test
    void contextLoads() {
        assertNotNull(usuarioService);
    }

    @Test
    void deveValidarRegrasDeNegocioEFluxoCompletoComMapa() {
        // RN01: E-mail fora do domínio institucional deve falhar
        CadastroRequestDTO invalido = new CadastroRequestDTO();
        invalido.setNome("Externo");
        invalido.setEmailInstitucional("externo@gmail.com");
        invalido.setProntuario("VP0001");
        invalido.setCurso("ADS");
        invalido.setSenha("123");
        invalido.setTipoUsuario("PASSAGEIRO");
        assertThrows(RegraNegocioException.class, () -> usuarioService.cadastrarUsuario(invalido));

        // Cadastro de Condutor válido
        CadastroRequestDTO condDto = new CadastroRequestDTO();
        condDto.setNome("Carlos Condutor");
        condDto.setEmailInstitucional("carlos.teste@aluno.ifsp.edu.br");
        condDto.setProntuario("VP1001");
        condDto.setCurso("BSI");
        condDto.setSenha("123456");
        condDto.setTipoUsuario("CONDUTOR");
        condDto.setCnh("99887766554");
        Usuario condutor = usuarioService.cadastrarUsuario(condDto);

        // Cadastro de Passageiro válido
        CadastroRequestDTO passDto = new CadastroRequestDTO();
        passDto.setNome("Ana Passageira");
        passDto.setEmailInstitucional("ana.teste@aluno.ifsp.edu.br");
        passDto.setProntuario("VP1002");
        passDto.setCurso("BSI");
        passDto.setSenha("123456");
        passDto.setTipoUsuario("PASSAGEIRO");
        Usuario passageiro = usuarioService.cadastrarUsuario(passDto);

        // RN08: Cadastro de veículo e bloqueio de placa duplicada
        VeiculoRequestDTO vDto = new VeiculoRequestDTO();
        vDto.setModelo("Onix");
        vDto.setCor("Prata");
        vDto.setPlaca("ABC1D23");
        vDto.setCapacidadePassageiros(4);
        Veiculo veiculo = veiculoService.cadastrarVeiculo(condutor.getId(), vDto);
        assertThrows(RegraNegocioException.class, () -> veiculoService.cadastrarVeiculo(condutor.getId(), vDto));

        // Publicação de Rota com Coordenadas de Mapa
        PublicacaoRotaRequestDTO rotaDto = new PublicacaoRotaRequestDTO();
        rotaDto.setBairroOrigem("Vila América");
        rotaDto.setDestinoCampus("IFSP Campus Votuporanga");
        rotaDto.setDiasSemana(List.of("Seg", "Qua"));
        rotaDto.setHorarioHabitual(LocalTime.of(7, 30));
        rotaDto.setVeiculoId(veiculo.getId());
        rotaDto.setLatitudeOrigem(-20.4180);
        rotaDto.setLongitudeOrigem(-49.9750);
        rotaDto.setLatitudeDestino(-20.4034);
        rotaDto.setLongitudeDestino(-49.9678);
        rotaDto.setDistanciaEstimada("2.8 km");
        rotaDto.setDuracaoEstimada("7 min");

        PublicacaoRota rota = publicacaoRotaService.publicarRota(condutor.getId(), rotaDto);
        assertEquals(-20.4180, rota.getLatitudeOrigem());
        assertEquals("2.8 km", rota.getDistanciaEstimada());

        // RN05: Autor não pode solicitar conexão na própria rota
        assertThrows(RegraNegocioException.class, () -> conexaoService.solicitarConexao(rota.getId(), condutor.getId()));

        // Solicitação válida e RN07 (bloqueio de duplicidade)
        Conexao conexao = conexaoService.solicitarConexao(rota.getId(), passageiro.getId());
        assertEquals("SOLICITADO", conexao.getStatus());
        assertThrows(RegraNegocioException.class, () -> conexaoService.solicitarConexao(rota.getId(), passageiro.getId()));

        // RN04: Chat bloqueado (envio e listagem) enquanto status for SOLICITADO
        assertThrows(EstadoInvalidoException.class,
                () -> mensagemChatService.enviarMensagem(conexao.getId(), passageiro.getId(), "Olá!"));
        assertThrows(EstadoInvalidoException.class,
                () -> mensagemChatService.listarMensagensPorConexao(conexao.getId(), passageiro.getId()));

        // Aceitar conexão libera o chat (envio e listagem)
        conexaoService.aceitarConexao(conexao.getId(), condutor.getId());
        var msgSalva = mensagemChatService.enviarMensagem(conexao.getId(), passageiro.getId(), "Combinado no mapa!");
        assertNotNull(msgSalva);
        assertEquals(1, mensagemChatService.listarMensagensPorConexao(conexao.getId(), condutor.getId()).size());

        // Verificação de segurança contra StackOverflowError em toString/equals/hashCode nas associações bidirecionais
        assertDoesNotThrow(() -> {
            assertNotNull(condutor.toString());
            assertNotNull(veiculo.toString());
            assertNotNull(rota.toString());
            assertNotNull(conexao.toString());
            assertNotNull(msgSalva.toString());
            assertNotEquals(0, condutor.hashCode());
            assertNotEquals(0, veiculo.hashCode());
        });
    }

    @Test
    void devePermitirCadastroELoginCaseInsensitiveComTratamentoDeEspacos() {
        CadastroRequestDTO dto = new CadastroRequestDTO();
        dto.setNome("  Maria de Fátima  ");
        dto.setEmailInstitucional("Maria.Fatima@Aluno.IFSP.edu.br ");
        dto.setProntuario("  bg309999 ");
        dto.setCurso("  TSI ");
        dto.setSenha("123456");
        dto.setTipoUsuario("PASSAGEIRO");

        Usuario u = usuarioService.cadastrarUsuario(dto);
        assertNotNull(u.getId());
        assertEquals("maria.fatima@aluno.ifsp.edu.br", u.getEmailInstitucional());
        assertEquals("BG309999", u.getProntuario());

        // Login com email com letras minúsculas/maiúsculas mistas
        com.example.ifrotas.dto.auth.LoginRequestDTO loginEmail = new com.example.ifrotas.dto.auth.LoginRequestDTO();
        loginEmail.setIdentificador("  MARIA.FATIMA@aluno.ifsp.edu.br ");
        loginEmail.setSenha("123456");
        Usuario auth1 = usuarioService.autenticar(loginEmail);
        assertEquals(u.getId(), auth1.getId());

        // Login com prontuário em minúsculas
        com.example.ifrotas.dto.auth.LoginRequestDTO loginPront = new com.example.ifrotas.dto.auth.LoginRequestDTO();
        loginPront.setIdentificador("bg309999");
        loginPront.setSenha("123456");
        Usuario auth2 = usuarioService.autenticar(loginPront);
        assertEquals(u.getId(), auth2.getId());
    }

    @Test
    void deveListarVeiculosSemNullPointerEDetectarPlacaDuplicadaCaseInsensitive() {
        CadastroRequestDTO cDto = new CadastroRequestDTO();
        cDto.setNome("Condutor Teste Veiculo");
        cDto.setEmailInstitucional("cond.veic@aluno.ifsp.edu.br");
        cDto.setProntuario("BG308888");
        cDto.setCurso("Física");
        cDto.setSenha("123");
        cDto.setTipoUsuario("CONDUTOR");
        cDto.setCnh("12345678901");
        Usuario condutor = usuarioService.cadastrarUsuario(cDto);

        // Deve retornar lista vazia sem lançar NullPointerException
        List<Veiculo> veiculosIniciais = veiculoService.listarVeiculosDoCondutor(condutor.getId());
        assertNotNull(veiculosIniciais);
        assertTrue(veiculosIniciais.isEmpty());

        // Cadastra veículo com placa em minúsculas
        VeiculoRequestDTO v1 = new VeiculoRequestDTO();
        v1.setModelo("HB20");
        v1.setCor("Branco");
        v1.setPlaca("xyz9k88");
        v1.setCapacidadePassageiros(4);
        Veiculo salvo = veiculoService.cadastrarVeiculo(condutor.getId(), v1);
        assertEquals("XYZ9K88", salvo.getPlaca());

        // Tentativa de duplicar com placa em maiúsculas deve ser barrada pela RN08
        VeiculoRequestDTO v2 = new VeiculoRequestDTO();
        v2.setModelo("HB20 2");
        v2.setCor("Prata");
        v2.setPlaca("XYZ9K88");
        v2.setCapacidadePassageiros(4);
        assertThrows(RegraNegocioException.class, () -> veiculoService.cadastrarVeiculo(condutor.getId(), v2));

        // Listagem agora deve conter exatamente 1 veículo
        List<Veiculo> veiculosApos = veiculoService.listarVeiculosDoCondutor(condutor.getId());
        assertEquals(1, veiculosApos.size());
    }

    @Test
    void deveFiltrarRotasPorTurnoComAcentuacao() {
        CadastroRequestDTO cDto = new CadastroRequestDTO();
        cDto.setNome("Condutor Rota Turno");
        cDto.setEmailInstitucional("cond.turno@aluno.ifsp.edu.br");
        cDto.setProntuario("BG307777");
        cDto.setCurso("Matemática");
        cDto.setSenha("123");
        cDto.setTipoUsuario("CONDUTOR");
        cDto.setCnh("98765432100");
        Usuario condutor = usuarioService.cadastrarUsuario(cDto);

        VeiculoRequestDTO vDto = new VeiculoRequestDTO();
        vDto.setModelo("Gol");
        vDto.setCor("Vermelho");
        vDto.setPlaca("TUR1N01");
        vDto.setCapacidadePassageiros(3);
        Veiculo v = veiculoService.cadastrarVeiculo(condutor.getId(), vDto);

        PublicacaoRotaRequestDTO rota = new PublicacaoRotaRequestDTO();
        rota.setBairroOrigem("Parque das Árvores");
        rota.setDestinoCampus("IFSP Birigui");
        rota.setDiasSemana(List.of("Seg", "Ter"));
        rota.setHorarioHabitual(LocalTime.of(7, 15));
        rota.setVeiculoId(v.getId());
        PublicacaoRota rotaCriada = publicacaoRotaService.publicarRota(condutor.getId(), rota);

        // Filtro com "manhã" com acento e "manha" sem acento
        assertFalse(publicacaoRotaService.buscarRotasAtivas(null, "manhã", null).isEmpty());
        assertFalse(publicacaoRotaService.buscarRotasAtivas(null, "manha", null).isEmpty());

        // Filtro de bairro e curso ignorando acentuação e caixa
        assertFalse(publicacaoRotaService.buscarRotasAtivas("arvores", null, "matematica").isEmpty());
        assertFalse(publicacaoRotaService.buscarRotasAtivas("ÁRVORES", null, "MATEMÁTICA").isEmpty());

        // RN06: Bloquear aceite de conexão quando a rota já foi inativada
        CadastroRequestDTO pDto = new CadastroRequestDTO();
        pDto.setNome("Passageiro Rota Inativa");
        pDto.setEmailInstitucional("pass.inativa@aluno.ifsp.edu.br");
        pDto.setProntuario("BG307778");
        pDto.setCurso("Física");
        pDto.setSenha("123");
        pDto.setTipoUsuario("PASSAGEIRO");
        Usuario pass = usuarioService.cadastrarUsuario(pDto);

        Conexao conPendente = conexaoService.solicitarConexao(rotaCriada.getId(), pass.getId());
        publicacaoRotaService.inativarRota(rotaCriada.getId(), condutor.getId());

        assertThrows(EstadoInvalidoException.class,
                () -> conexaoService.aceitarConexao(conPendente.getId(), condutor.getId()));
    }
}
