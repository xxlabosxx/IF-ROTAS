// Estado global
let currentUser = null;
let currentChatInterval = null;
let cachedRotas = [];
let cachedConexoes = [];

// Perfis pré-definidos para o modo de Debug e Testes (Foco IFSP Birigui)
const PERFIS_DEBUG = {
    condutor1: {
        tipo: 'CONDUTOR',
        rotulo: 'Condutor 1',
        nome: 'Lucas Martins',
        emailInstitucional: 'condutor1@aluno.ifsp.edu.br',
        prontuario: 'BG301101',
        curso: 'Sistemas para Internet',
        senha: '123',
        cnh: '11122233344',
        minibiografia: 'Estudante do curso de Sistemas para Internet no IFSP Birigui. Faço o trajeto no período noturno.',
        preferenciasConvivencia: 'Gosto de ouvir MPB e podcasts de tecnologia durante a viagem.',
        veiculo: {
            modelo: 'Chevrolet Onix LT 1.0',
            cor: 'Prata',
            placa: 'BRA1C01',
            capacidadePassageiros: 4
        },
        rotaSugerida: {
            bairroOrigem: 'Parque das Árvores, Birigui',
            destinoCampus: 'IFSP Campus Birigui',
            diasSemana: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex'],
            horarioHabitual: '18:45',
            latitudeOrigem: -21.2825,
            longitudeOrigem: -50.3340,
            latitudeDestino: -21.2673,
            longitudeDestino: -50.3204,
            distanciaEstimada: '2.5 km',
            duracaoEstimada: '6 min'
        }
    },
    condutor2: {
        tipo: 'CONDUTOR',
        rotulo: 'Condutor 2',
        nome: 'Mariana Zanetti',
        emailInstitucional: 'condutor2@aluno.ifsp.edu.br',
        prontuario: 'BG301102',
        curso: 'Licenciatura em Física',
        senha: '123',
        cnh: '22233344455',
        minibiografia: 'Futura professora de Física, vou diariamente ao campus no período da manhã saindo de Silvares.',
        preferenciasConvivencia: 'Trajeto silencioso e calmo, bom para ler ou estudar no caminho.',
        veiculo: {
            modelo: 'Hyundai HB20 Comfort',
            cor: 'Branco',
            placa: 'BRA2C02',
            capacidadePassageiros: 3
        },
        rotaSugerida: {
            bairroOrigem: 'Silvares, Birigui',
            destinoCampus: 'IFSP Campus Birigui',
            diasSemana: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex'],
            horarioHabitual: '07:15',
            latitudeOrigem: -21.2912,
            longitudeOrigem: -50.3421,
            latitudeDestino: -21.2673,
            longitudeDestino: -50.3204,
            distanciaEstimada: '3.8 km',
            duracaoEstimada: '8 min'
        }
    },
    condutor3: {
        tipo: 'CONDUTOR',
        rotulo: 'Condutor 3',
        nome: 'Gabriel Rocha',
        emailInstitucional: 'condutor3@aluno.ifsp.edu.br',
        prontuario: 'BG301103',
        curso: 'Mecatrônica Industrial',
        senha: '123',
        cnh: '33344455566',
        minibiografia: 'Estágio à tarde em Birigui e vou para o IFSP Birigui à noite saindo do Jardim Pérola.',
        preferenciasConvivencia: 'Carro com ar-condicionado, trajeto descontraído e boas conversas.',
        veiculo: {
            modelo: 'Volkswagen Polo TSI',
            cor: 'Cinza',
            placa: 'BRA3C03',
            capacidadePassageiros: 4
        },
        rotaSugerida: {
            bairroOrigem: 'Jardim Pérola, Birigui',
            destinoCampus: 'IFSP Campus Birigui',
            diasSemana: ['Seg', 'Qua', 'Sex'],
            horarioHabitual: '18:30',
            latitudeOrigem: -21.2750,
            longitudeOrigem: -50.3120,
            latitudeDestino: -21.2673,
            longitudeDestino: -50.3204,
            distanciaEstimada: '1.9 km',
            duracaoEstimada: '4 min'
        }
    },
    passageiro1: {
        tipo: 'PASSAGEIRO',
        rotulo: 'Passageiro Teste',
        nome: 'Ana Clara',
        emailInstitucional: 'passageiro@aluno.ifsp.edu.br',
        prontuario: 'BG302001',
        curso: 'Administração',
        senha: '123',
        minibiografia: 'Moro no Centro de Birigui e busco carona para o período noturno no IFSP.',
        preferenciasConvivencia: 'Pontual e tranquila.'
    }
};

// Estado do mapa de criação de rota (IFSP Campus Birigui como centro padrão)
let novaRotaMapState = {
    map: null,
    markerOrigem: null,
    markerDestino: null,
    routeLayer: null,
    latOrigem: null,
    lngOrigem: null,
    latDestino: -21.2673,
    lngDestino: -50.3204,
    distanciaEstimada: null,
    duracaoEstimada: null
};

// Estado do modal de visualização de mapa
let modalMapState = {
    map: null,
    markerOrigem: null,
    markerDestino: null,
    markerUser: null,
    routeLayer: null,
    currentDados: null
};

// Roteador simples
const routes = {
    '#login': renderLogin,
    '#feed': renderFeed,
    '#rotas/nova': renderNovaRota,
    '#perfil': renderPerfil,
    '#conexoes': renderConexoes
};

function navigate() {
    if (!window.location.hash && window.location.pathname && window.location.pathname !== '/' && window.location.pathname !== '/index.html') {
        const cleanPath = window.location.pathname.replace(/^\/+/, '');
        window.history.replaceState(null, '', '/#' + cleanPath);
    }
    let hash = window.location.hash || '#login';
    
    // Cancela polling de chat se navegou para fora
    if (currentChatInterval) {
        clearInterval(currentChatInterval);
        currentChatInterval = null;
    }

    // Proteção de rota
    if (!currentUser && !hash.startsWith('#login')) {
        window.location.hash = '#login';
        return;
    }
    
    if (currentUser && hash === '#login') {
        window.location.hash = '#feed';
        return;
    }

    const contentArea = document.getElementById('content-area');
    contentArea.innerHTML = ''; // Limpa

    // Suporte a rota com parâmetros (ex: #chat/1)
    let routeHandler = routes[hash];
    let param = null;

    if (hash.startsWith('#chat/')) {
        routeHandler = renderChat;
        param = hash.split('/')[1];
    }

    if (routeHandler) {
        routeHandler(contentArea, param);
    }

    // Toggle navbar
    document.getElementById('navbar').style.display = currentUser ? 'flex' : 'none';
    atualizarInfoDebugBar();
}

// Inicialização
window.addEventListener('hashchange', navigate);
window.addEventListener('DOMContentLoaded', async () => {
    // Configura botões de controle da Debug Toolbar
    configurarControlesDebugToolbar();

    // Tenta recuperar sessão, garantindo que se o servidor reiniciou limpo, a sessão antiga é descartada
    const savedUser = localStorage.getItem('ifrotas_user');
    if (savedUser) {
        try {
            const parsed = JSON.parse(savedUser);
            // Testa se o usuário ainda existe no banco atual em memória
            const checkRes = await fetch(`/api/usuarios/${parsed.id}`).catch(() => null);
            if (checkRes && checkRes.ok) {
                currentUser = parsed;
                atualizarBadgeNotificacoes();
            } else {
                // Servidor reiniciou ou usuário não existe mais: limpa a sessão para começar deslogado
                currentUser = null;
                localStorage.removeItem('ifrotas_user');
            }
        } catch (e) {
            currentUser = null;
            localStorage.removeItem('ifrotas_user');
        }
    } else {
        currentUser = null;
    }

    navigate();
    atualizarInfoDebugBar();
    setInterval(atualizarBadgeNotificacoes, 15000);

    document.getElementById('btn-logout').addEventListener('click', (e) => {
        e.preventDefault();
        encerrarSessaoLocal();
        window.location.hash = '#login';
        atualizarInfoDebugBar();
    });

    // Mobile Menu Toggle
    document.getElementById('mobile-menu-btn').addEventListener('click', () => {
        document.getElementById('nav-links').classList.toggle('show');
    });

    // Fechar menu mobile ao clicar num link
    document.querySelectorAll('.nav-item').forEach(link => {
        link.addEventListener('click', () => {
            document.getElementById('nav-links').classList.remove('show');
        });
    });

    // Notification Dropdown Toggle
    document.getElementById('btn-notifications').addEventListener('click', () => {
        const drop = document.getElementById('notif-dropdown');
        drop.style.display = drop.style.display === 'none' ? 'block' : 'none';
        
        if (drop.style.display === 'block') {
            atualizarBadgeNotificacoes();
        }
    });

    // Fallback de light-dismiss para navegadores sem suporte nativo a <dialog closedby="any">
    if (!('closedBy' in HTMLDialogElement.prototype)) {
        const outside = (e, dialog) => {
            const r = dialog.getBoundingClientRect();
            return e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom;
        };
        for (const dialog of document.querySelectorAll('dialog[closedby="any"]')) {
            let pressedOutside = false;
            dialog.addEventListener('pointerdown', (e) => { pressedOutside = outside(e, dialog); });
            dialog.addEventListener('pointerup', (e) => { if (pressedOutside && outside(e, dialog)) dialog.close(); });
        }
    }

    const btnModalLoc = document.getElementById('btn-modal-minha-loc');
    if (btnModalLoc) {
        btnModalLoc.addEventListener('click', mostrarMinhaLocalizacaoNoModal);
    }
});

// ============================================================================
// FUNÇÕES DO PAINEL DE DEBUG (Login Facilitado e Ferramentas - SEM EMOJIS)
// ============================================================================

function setDebugStatusMsg(msg, isError = false) {
    const el = document.getElementById('debug-status-msg');
    if (!el) return;
    el.style.color = isError ? '#ef4444' : '#10b981';
    el.innerText = msg;
    if (msg) {
        setTimeout(() => {
            if (el.innerText === msg) el.innerText = '';
        }, 6000);
    }
}

function debugToggleSize() {
    const toolbar = document.getElementById('debug-toolbar');
    const sizeText = document.getElementById('debug-size-text');
    if (!toolbar) return;
    const collapsed = toolbar.classList.toggle('collapsed');
    sessionStorage.setItem('ifrotas_debug_collapsed', collapsed ? 'true' : 'false');
    if (sizeText) sizeText.innerText = collapsed ? 'Aumentar' : 'Reduzir';
}
window.debugToggleSize = debugToggleSize;

function debugClose() {
    const toolbar = document.getElementById('debug-toolbar');
    const reopenBtn = document.getElementById('debug-reopen-btn');
    if (toolbar) toolbar.classList.add('hidden');
    if (reopenBtn) reopenBtn.style.display = 'block';
    sessionStorage.setItem('ifrotas_debug_hidden', 'true');
}
window.debugClose = debugClose;

function debugReopen() {
    const toolbar = document.getElementById('debug-toolbar');
    const reopenBtn = document.getElementById('debug-reopen-btn');
    if (toolbar) toolbar.classList.remove('hidden');
    if (reopenBtn) reopenBtn.style.display = 'none';
    sessionStorage.removeItem('ifrotas_debug_hidden');
}
window.debugReopen = debugReopen;

function configurarControlesDebugToolbar() {
    const toolbar = document.getElementById('debug-toolbar');
    const reopenBtn = document.getElementById('debug-reopen-btn');
    const sizeText = document.getElementById('debug-size-text');

    if (!toolbar) return;

    const isCollapsed = sessionStorage.getItem('ifrotas_debug_collapsed') === 'true';
    const isHidden = sessionStorage.getItem('ifrotas_debug_hidden') === 'true';

    if (isCollapsed) {
        toolbar.classList.add('collapsed');
        if (sizeText) sizeText.innerText = 'Aumentar';
    } else {
        toolbar.classList.remove('collapsed');
        if (sizeText) sizeText.innerText = 'Reduzir';
    }

    if (isHidden) {
        toolbar.classList.add('hidden');
        if (reopenBtn) reopenBtn.style.display = 'block';
    } else {
        toolbar.classList.remove('hidden');
        if (reopenBtn) reopenBtn.style.display = 'none';
    }
}

function atualizarInfoDebugBar() {
    const statusEl = document.getElementById('debug-user-status');
    if (!statusEl) return;
    if (currentUser) {
        statusEl.innerHTML = `Logado: <strong>${escapeHtml(currentUser.nome)}</strong>`;
        statusEl.style.color = '#10b981';
    } else {
        statusEl.innerHTML = `Deslogado`;
        statusEl.style.color = '#94a3b8';
    }
}

async function debugLogin(perfilKey) {
    const perfil = PERFIS_DEBUG[perfilKey];
    if (!perfil) return;

    setDebugStatusMsg(`Autenticando ${perfil.rotulo}...`);

    try {
        // 1. Tenta login direto
        let resLogin = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identificador: perfil.emailInstitucional, senha: perfil.senha })
        });

        // 2. Se banco estiver limpo ou conta não existir, cria automaticamente
        if (!resLogin.ok) {
            setDebugStatusMsg(`Criando conta para ${perfil.rotulo}...`);
            const cadastroPayload = {
                nome: perfil.nome,
                emailInstitucional: perfil.emailInstitucional,
                prontuario: perfil.prontuario,
                curso: perfil.curso,
                senha: perfil.senha,
                tipoUsuario: perfil.tipo,
                cnh: perfil.cnh || null
            };

            const resCad = await fetch('/api/auth/cadastro', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cadastroPayload)
            });

            if (!resCad.ok) {
                const erroTxt = await resCad.text();
                setDebugStatusMsg(`Erro no cadastro: ${erroTxt}`, true);
                alert(`Erro no cadastro de ${perfil.rotulo}: ${erroTxt}`);
                return;
            }

            // Faz login após cadastro
            resLogin = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identificador: perfil.emailInstitucional, senha: perfil.senha })
            });

            if (!resLogin.ok) {
                setDebugStatusMsg(`Erro ao autenticar ${perfil.rotulo}`, true);
                return;
            }

            const novoUsuario = await resLogin.json();

            // Cadastra veículo se for condutor
            if (perfil.veiculo) {
                await fetch(`/api/condutores/${novoUsuario.id}/veiculos`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(perfil.veiculo)
                }).catch(() => null);
            }

            // Atualiza preferências e bio
            if (perfil.minibiografia || perfil.preferenciasConvivencia) {
                await fetch(`/api/usuarios/${novoUsuario.id}/perfil`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        minibiografia: perfil.minibiografia,
                        preferenciasConvivencia: perfil.preferenciasConvivencia
                    })
                }).catch(() => null);
            }

            currentUser = novoUsuario;
        } else {
            currentUser = await resLogin.json();
        }

        // Salva na sessão e atualiza tela
        localStorage.setItem('ifrotas_user', JSON.stringify(currentUser));
        atualizarInfoDebugBar();
        atualizarBadgeNotificacoes();

        const navEl = document.getElementById('navbar');
        if (navEl) navEl.style.display = 'flex';

        window.location.hash = '#feed';
        navigate();
        setDebugStatusMsg(`Conectado como ${currentUser.nome}`);
    } catch (err) {
        console.error('Erro no login de debug:', err);
        setDebugStatusMsg('Erro de comunicacao com o servidor', true);
        alert('Nao foi possivel conectar ao servidor backend. Verifique se o servidor esta ativo.');
    }
}
window.debugLogin = debugLogin;

async function debugPublicarRotaBirigui() {
    if (!currentUser) {
        setDebugStatusMsg('Faca login primeiro como condutor', true);
        alert('Faca login primeiro clicando em Condutor 1, 2 ou 3 na barra de debug.');
        return;
    }

    setDebugStatusMsg('Publicando rota em Birigui...');

    try {
        const resVeic = await fetch(`/api/condutores/${currentUser.id}/veiculos`);
        let veiculoId = null;
        if (resVeic.ok) {
            const veiculos = await resVeic.json();
            if (veiculos.length > 0) {
                veiculoId = veiculos[0].id;
            }
        }

        let rotaInfo = null;
        for (const key of Object.keys(PERFIS_DEBUG)) {
            if (PERFIS_DEBUG[key].emailInstitucional === currentUser.emailInstitucional && PERFIS_DEBUG[key].rotaSugerida) {
                rotaInfo = PERFIS_DEBUG[key].rotaSugerida;
                break;
            }
        }

        if (!rotaInfo) {
            rotaInfo = {
                bairroOrigem: 'Residencial Portal da Pérola, Birigui',
                destinoCampus: 'IFSP Campus Birigui',
                diasSemana: ['Seg', 'Ter', 'Qua', 'Qui', 'Sex'],
                horarioHabitual: '19:00',
                latitudeOrigem: -21.2650,
                longitudeOrigem: -50.3150,
                latitudeDestino: -21.2673,
                longitudeDestino: -50.3204,
                distanciaEstimada: '1.2 km',
                duracaoEstimada: '3 min'
            };
        }

        const payload = {
            ...rotaInfo,
            veiculoId: veiculoId
        };

        const res = await fetch(`/api/rotas?autorId=${currentUser.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            setDebugStatusMsg(`Rota publicada (${rotaInfo.bairroOrigem})`);
            if (window.location.hash === '#feed') {
                carregarRotas();
            } else {
                window.location.hash = '#feed';
                navigate();
            }
        } else {
            const erroMsg = await res.text();
            setDebugStatusMsg(`Erro ao publicar rota: ${erroMsg}`, true);
            alert(`Atencao: ${erroMsg}`);
        }
    } catch (e) {
        console.error(e);
        setDebugStatusMsg('Erro ao publicar rota', true);
    }
}
window.debugPublicarRotaBirigui = debugPublicarRotaBirigui;

function encerrarSessaoLocal() {
    currentUser = null;
    cachedRotas = [];
    cachedConexoes = [];
    localStorage.removeItem('ifrotas_user');
    const badge = document.getElementById('notif-badge');
    const drop = document.getElementById('notif-dropdown');
    const notifList = document.getElementById('notif-list');
    if (badge) badge.style.display = 'none';
    if (drop) drop.style.display = 'none';
    if (notifList) notifList.innerHTML = '<p class="empty-state" style="padding: 1rem 0;">Sem notificações no momento.</p>';
}

function debugDeslogar() {
    encerrarSessaoLocal();
    window.location.hash = '#login';
    navigate();
    atualizarInfoDebugBar();
    setDebugStatusMsg('Sessao encerrada');
}
window.debugDeslogar = debugDeslogar;

function debugResetarTudo() {
    if (confirm('Deseja limpar todos os dados salvos em cache local (localStorage e sessionStorage)?')) {
        localStorage.clear();
        sessionStorage.clear();
        currentUser = null;
        window.location.hash = '#login';
        window.location.reload();
    }
}
window.debugResetarTudo = debugResetarTudo;

async function atualizarBadgeNotificacoes() {
    if (!currentUser) return;
    try {
        const res = await fetch(`/api/conexoes?usuarioId=${currentUser.id}`);
        if (!res.ok) return;
        const conexoes = await res.json();
        const pendentes = conexoes.filter(c => c.autorId === currentUser.id && c.status === 'SOLICITADO');
        
        const badge = document.getElementById('notif-badge');
        const notifList = document.getElementById('notif-list');
        if (!badge || !notifList) return;
        
        if (pendentes.length > 0) {
            badge.style.display = 'block';
            badge.innerText = pendentes.length;
            notifList.innerHTML = pendentes.map(p => `
                <div style="font-size: 0.875rem; margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid var(--surface-border);">
                    <strong>${escapeHtml(p.interessadoNome || 'Alguém')}</strong> solicitou carona para <em>${escapeHtml(p.rotaDestino || '')}</em>.<br>
                    <a href="#conexoes" style="color: var(--primary); text-decoration: none; font-size: 0.8rem; font-weight: 600;">Ver no painel &rarr;</a>
                </div>
            `).join('');
        } else {
            badge.style.display = 'none';
            notifList.innerHTML = '<p class="empty-state" style="padding: 1rem 0;">Sem notificações no momento.</p>';
        }
    } catch(e) {
        // Silencia erro eventual de polling
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function formatarData(dataStr) {
    if (!dataStr) return '';
    try {
        const d = new Date(dataStr);
        return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        return dataStr;
    }
}

// Renderizadores
function renderLogin(container) {
    const template = document.getElementById('tpl-auth').content.cloneNode(true);
    container.appendChild(template);

    const tabs = container.querySelectorAll('.tab');
    const formLogin = container.querySelector('#form-login');
    const formCadastro = container.querySelector('#form-cadastro');
    const selTipo = container.querySelector('#cad-tipo');
    const groupCnh = container.querySelector('#group-cnh');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            if (tab.dataset.target === 'login') {
                formLogin.style.display = 'block';
                formCadastro.style.display = 'none';
            } else {
                formLogin.style.display = 'none';
                formCadastro.style.display = 'block';
            }
        });
    });

    selTipo.addEventListener('change', (e) => {
        groupCnh.style.display = e.target.value === 'CONDUTOR' ? 'block' : 'none';
    });

    formLogin.addEventListener('submit', async (e) => {
        e.preventDefault();
        const ident = document.getElementById('login-id').value;
        const senha = document.getElementById('login-senha').value;
        
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ identificador: ident, senha: senha })
            });
            if(res.ok) {
                currentUser = await res.json();
                localStorage.setItem('ifrotas_user', JSON.stringify(currentUser));
                atualizarBadgeNotificacoes();
                window.location.hash = '#feed';
            } else {
                const msg = await res.text();
                alert(msg || 'Credenciais inválidas');
            }
        } catch(err) {
            console.error(err);
            alert('Erro ao tentar conectar.');
        }
    });

    formCadastro.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            nome: document.getElementById('cad-nome').value,
            emailInstitucional: document.getElementById('cad-email').value,
            prontuario: document.getElementById('cad-prontuario').value,
            curso: document.getElementById('cad-curso').value,
            senha: document.getElementById('cad-senha').value,
            tipoUsuario: document.getElementById('cad-tipo').value,
            cnh: document.getElementById('cad-cnh').value
        };

        try {
            const res = await fetch('/api/auth/cadastro', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
            if(res.ok) {
                alert('Cadastro realizado! Faça login.');
                formCadastro.reset();
                groupCnh.style.display = 'none';
                tabs[0].click(); // Volta pro login
            } else {
                const msg = await res.text();
                alert('Erro: ' + msg);
            }
        } catch(err) {
            console.error(err);
        }
    });
}

function renderFeed(container) {
    const template = document.getElementById('tpl-feed').content.cloneNode(true);
    container.appendChild(template);
    
    const inputBairro = document.getElementById('filtro-bairro');
    const inputCurso = document.getElementById('filtro-curso');
    const selTurno = document.getElementById('filtro-turno');
    const btnFiltrar = document.getElementById('btn-filtrar');
    const btnLimpar = document.getElementById('btn-limpar-filtros');

    const dispararFiltro = () => {
        carregarRotas(inputBairro.value, selTurno.value, inputCurso.value);
    };

    if (btnFiltrar) btnFiltrar.addEventListener('click', dispararFiltro);
    if (inputBairro) inputBairro.addEventListener('keyup', (e) => { if (e.key === 'Enter') dispararFiltro(); });
    if (inputCurso) inputCurso.addEventListener('keyup', (e) => { if (e.key === 'Enter') dispararFiltro(); });
    if (selTurno) selTurno.addEventListener('change', dispararFiltro);

    if (btnLimpar) {
        btnLimpar.addEventListener('click', () => {
            if (inputBairro) inputBairro.value = '';
            if (inputCurso) inputCurso.value = '';
            if (selTurno) selTurno.value = '';
            carregarRotas();
        });
    }

    carregarRotas();
}

async function carregarRotas(bairro = '', turno = '', curso = '') {
    try {
        const params = new URLSearchParams();
        if (bairro && bairro.trim()) params.append('bairro', bairro.trim());
        if (turno && turno.trim()) params.append('turno', turno.trim());
        if (curso && curso.trim()) params.append('curso', curso.trim());

        const url = '/api/rotas' + (params.toString() ? '?' + params.toString() : '');
        const res = await fetch(url);
        if (res.ok) {
            const rotas = await res.json();
            cachedRotas = rotas;
            const grid = document.getElementById('rotas-list');
            if (!grid) return;
            grid.innerHTML = '';
            
            if (rotas.length === 0) {
                grid.innerHTML = '<p class="empty-state">Nenhuma rota encontrada com os filtros informados.</p>';
                return;
            }

            rotas.forEach(rota => {
                const card = document.createElement('div');
                card.className = 'rota-card';
                const isMinhaRota = rota.autor && currentUser && rota.autor.id === currentUser.id;
                const infoTrajeto = (rota.distanciaEstimada || rota.duracaoEstimada)
                    ? `<div style="font-size: 0.8rem; color: var(--primary); margin-bottom: 0.5rem; font-weight: 500;">
                         🛣️ ${escapeHtml(rota.distanciaEstimada || '')} ${rota.duracaoEstimada ? '• ⏱️ ~' + escapeHtml(rota.duracaoEstimada) : ''}
                       </div>`
                    : '';
                const infoVeiculo = rota.veiculo
                    ? `<div style="font-size: 0.78rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
                         🚗 ${escapeHtml(rota.veiculo.modelo)} (${escapeHtml(rota.veiculo.cor)}) • Placa: ${escapeHtml(rota.veiculo.placa)} • ${escapeHtml(rota.veiculo.capacidadePassageiros)} vagas
                       </div>`
                    : '';
                const infoConvivencia = (rota.autor?.minibiografia || rota.autor?.preferenciasConvivencia)
                    ? `<div style="font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.75rem; padding: 0.45rem 0.6rem; background: rgba(255,255,255,0.03); border-radius: 6px; border-left: 2px solid var(--primary);">
                         ${rota.autor.minibiografia ? `<div>💬 ${escapeHtml(rota.autor.minibiografia)}</div>` : ''}
                         ${rota.autor.preferenciasConvivencia ? `<div style="margin-top: 2px;">🎵 ${escapeHtml(rota.autor.preferenciasConvivencia)}</div>` : ''}
                       </div>`
                    : '';
                const diasFormatados = (rota.diasSemana || []).map(d => escapeHtml(d)).join(', ');

                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; align-items: start;">
                        <div>
                            <div class="rota-dest">${escapeHtml(rota.destinoCampus)}</div>
                            <div class="rota-origem">Saindo de: ${escapeHtml(rota.bairroOrigem)}</div>
                        </div>
                        ${rota.veiculo ? '<span class="badge-oferta">Oferta</span>' : '<span class="badge-oferta" style="background: rgba(59, 130, 246, 0.1); color: var(--accent);">Pedido</span>'}
                    </div>
                    ${infoTrajeto}
                    ${infoVeiculo}
                    <div class="rota-meta">
                        <span>🕒 ${escapeHtml(rota.horarioHabitual || '')}</span>
                        <span>📅 ${diasFormatados}</span>
                    </div>
                    ${infoConvivencia}
                    <div style="display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                        <span style="font-size: 0.875rem; color: var(--text-secondary);">
                            👤 ${escapeHtml(rota.autor?.nome || 'Anônimo')} ${rota.autor?.curso ? '(' + escapeHtml(rota.autor.curso) + ')' : ''}
                        </span>
                        <div style="display: flex; gap: 0.5rem; align-items: center;">
                            <button type="button" class="btn-secondary btn-sm" onclick="abrirModalMapaPorRotaId(${rota.id})">🗺️ Trajeto</button>
                            ${isMinhaRota 
                                ? `<button type="button" class="btn-danger btn-sm" onclick="inativarMinhaRota(${rota.id})">✕ Inativar</button>` 
                                : `<button type="button" class="btn-primary btn-sm" onclick="solicitarConexao(${rota.id}, this)">Conectar</button>`}
                        </div>
                    </div>
                `;
                grid.appendChild(card);
            });
        }
    } catch(err) {
        console.error(err);
    }
}

async function solicitarConexao(rotaId, btnElement) {
    try {
        const res = await fetch(`/api/rotas/${rotaId}/conectar?usuarioInteressadoId=${currentUser.id}`, { method: 'POST' });
        if (res.ok) {
            alert('Solicitação enviada com sucesso!');
            const card = btnElement.closest('.rota-card');
            if (card) {
                card.style.transform = 'scale(0.9)';
                card.style.opacity = '0';
                setTimeout(() => card.remove(), 300);
            }
        } else {
            const msg = await res.text();
            alert('Atenção: ' + msg);
        }
    } catch(err) {
        console.error(err);
        alert('Erro ao enviar solicitação.');
    }
}

async function inativarMinhaRota(rotaId) {
    if (!confirm('Deseja realmente inativar esta publicação de rota? Ela deixará de aparecer nas buscas.')) return;
    try {
        const res = await fetch(`/api/rotas/${rotaId}/inativar?autorId=${currentUser.id}`, { method: 'PATCH' });
        if (res.ok) {
            alert('Rota inativada com sucesso.');
            const b = document.getElementById('filtro-bairro')?.value || '';
            const t = document.getElementById('filtro-turno')?.value || '';
            const c = document.getElementById('filtro-curso')?.value || '';
            carregarRotas(b, t, c);
        } else {
            const msg = await res.text();
            alert('Atenção: ' + msg);
        }
    } catch (err) {
        console.error(err);
        alert('Erro ao inativar rota.');
    }
}

function renderNovaRota(container) {
    const template = document.getElementById('tpl-nova-rota').content.cloneNode(true);
    container.appendChild(template);

    carregarVeiculosParaSelect();
    setTimeout(() => inicializarMapaNovaRota(), 50);

    const form = document.getElementById('form-nova-rota');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const payload = {
            bairroOrigem: document.getElementById('rota-bairro').value,
            destinoCampus: document.getElementById('rota-destino').value,
            diasSemana: document.getElementById('rota-dias').value.split(',').map(s => s.trim()).filter(Boolean),
            horarioHabitual: document.getElementById('rota-hora').value,
            veiculoId: document.getElementById('rota-veiculo').value || null,
            latitudeOrigem: novaRotaMapState.latOrigem,
            longitudeOrigem: novaRotaMapState.lngOrigem,
            latitudeDestino: novaRotaMapState.latDestino,
            longitudeDestino: novaRotaMapState.lngDestino,
            distanciaEstimada: novaRotaMapState.distanciaEstimada,
            duracaoEstimada: novaRotaMapState.duracaoEstimada
        };

        try {
            const res = await fetch(`/api/rotas?autorId=${currentUser.id}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                alert('Rota publicada com sucesso!');
                window.location.hash = '#feed';
            } else {
                const msg = await res.text();
                alert('Erro: ' + msg);
            }
        } catch(err) {
            console.error(err);
        }
    });
}

async function carregarVeiculosParaSelect() {
    if (!currentUser || currentUser.tipoUsuario !== 'CONDUTOR') {
        const grp = document.getElementById('grupo-veiculo-rota');
        if (grp) grp.style.display = 'none';
        return;
    }
    try {
        const res = await fetch(`/api/condutores/${currentUser.id}/veiculos`);
        if (res.ok) {
            const veiculos = await res.json();
            const grp = document.getElementById('grupo-veiculo-rota');
            const sel = document.getElementById('rota-veiculo');
            if (!grp || !sel) return;
            grp.style.display = 'block';
            if (veiculos.length > 0) {
                sel.innerHTML = veiculos.map((v, idx) =>
                    `<option value="${v.id}" ${idx === 0 ? 'selected' : ''}>${escapeHtml(v.modelo)} - ${escapeHtml(v.placa)} (${escapeHtml(v.capacidadePassageiros)} vagas)</option>`
                ).join('');
            } else {
                sel.innerHTML = '<option value="">Cadastre um veículo na aba Perfil antes de publicar</option>';
            }
        }
    } catch(err) {
        // Ignora
    }
}

function renderPerfil(container) {
    const template = document.getElementById('tpl-perfil').content.cloneNode(true);
    container.appendChild(template);

    // Preenche
    document.getElementById('perfil-bio').value = currentUser.minibiografia || '';
    document.getElementById('perfil-prefs').value = currentUser.preferenciasConvivencia || '';

    // Salvar perfil
    document.getElementById('form-perfil').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            minibiografia: document.getElementById('perfil-bio').value,
            preferenciasConvivencia: document.getElementById('perfil-prefs').value
        };
        try {
            const res = await fetch(`/api/usuarios/${currentUser.id}/perfil`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                currentUser = await res.json();
                localStorage.setItem('ifrotas_user', JSON.stringify(currentUser));
                alert('Perfil atualizado com sucesso!');
            } else {
                const msg = await res.text();
                alert('Erro ao atualizar perfil: ' + msg);
            }
        } catch(err) { console.error(err); }
    });

    // Registra o listener do formulário de veículos apenas uma vez por renderização de view
    const formVeiculo = document.getElementById('form-veiculo');
    if (formVeiculo) {
        formVeiculo.addEventListener('submit', async (e) => {
            e.preventDefault();
            const vPayload = {
                modelo: document.getElementById('veiculo-modelo').value,
                placa: document.getElementById('veiculo-placa').value,
                cor: document.getElementById('veiculo-cor').value,
                capacidadePassageiros: Number(document.getElementById('veiculo-cap').value)
            };
            try {
                const vRes = await fetch(`/api/condutores/${currentUser.id}/veiculos`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(vPayload)
                });
                if (vRes.ok) {
                    alert('Veículo cadastrado!');
                    e.target.reset();
                    await verificarEExibirVeiculos();
                } else {
                    alert('Erro ao cadastrar: ' + await vRes.text());
                }
            } catch(err) { console.error(err); }
        });
    }

    // Se for condutor, exibe e atualiza a lista de veículos
    verificarEExibirVeiculos();
}

async function verificarEExibirVeiculos() {
    if (!currentUser || currentUser.tipoUsuario !== 'CONDUTOR') {
        const area = document.getElementById('area-condutor');
        if (area) area.style.display = 'none';
        return;
    }
    try {
        const res = await fetch(`/api/condutores/${currentUser.id}/veiculos`);
        if (res.ok) {
            const area = document.getElementById('area-condutor');
            if (area) area.style.display = 'block';
            const veiculos = await res.json();
            const lista = document.getElementById('lista-veiculos');
            if (lista) {
                lista.innerHTML = veiculos.map(v =>
                    `<li>🚗 ${escapeHtml(v.modelo)} | ${escapeHtml(v.cor)} | Placa: ${escapeHtml(v.placa)} | ${escapeHtml(v.capacidadePassageiros)} lugares</li>`
                ).join('');
            }
        }
    } catch (e) {
        // Ignora
    }
}

async function renderConexoes(container) {
    const template = document.getElementById('tpl-conexoes').content.cloneNode(true);
    container.appendChild(template);

    await carregarConexoes();
}

async function carregarConexoes() {
    if (!currentUser) return;
    try {
        const res = await fetch(`/api/conexoes?usuarioId=${currentUser.id}`);
        if (!res.ok) {
            console.error('Falha ao consultar conexões.');
            return;
        }

        const conexoes = await res.json();
        cachedConexoes = conexoes;

        const listaRecebidas = document.getElementById('lista-recebidas');
        const listaAtivas = document.getElementById('lista-ativas');
        const listaEnviadas = document.getElementById('lista-enviadas');
        const badgeRecebidas = document.getElementById('badge-recebidas-count');

        // Categorizar
        const recebidas = conexoes.filter(c => c.autorId === currentUser.id && c.status === 'SOLICITADO');
        const ativas = conexoes.filter(c => c.status === 'CONECTADO');
        const enviadas = conexoes.filter(c => c.interessadoId === currentUser.id && c.status !== 'CONECTADO');

        // Atualiza contador da aba/seção
        if (badgeRecebidas) {
            if (recebidas.length > 0) {
                badgeRecebidas.innerText = recebidas.length;
                badgeRecebidas.style.display = 'inline-block';
            } else {
                badgeRecebidas.style.display = 'none';
            }
        }

        // 1. Solicitações Recebidas (onde sou condutor/autor da rota)
        if (listaRecebidas) {
            if (recebidas.length === 0) {
                listaRecebidas.innerHTML = '<p class="empty-state">Nenhuma solicitação pendente para suas rotas.</p>';
            } else {
                listaRecebidas.innerHTML = recebidas.map(c => `
                    <div class="conexao-card">
                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 0.5rem;">
                                <strong style="font-size: 1.1rem; color: var(--text-primary);">👤 ${escapeHtml(c.interessadoNome || 'Interessado')}</strong>
                                <span class="badge-solicitado">Pendente</span>
                            </div>
                            <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 0.5rem;">
                                Solicitou interesse na rota: <strong>${escapeHtml(c.rotaOrigem || 'Origem')} ➔ ${escapeHtml(c.rotaDestino || 'Destino')}</strong>
                            </p>
                            <small style="color: var(--text-secondary); font-size: 0.75rem;">📅 ${formatarData(c.dataSolicitacao)}</small>
                        </div>
                        <div class="conexao-actions">
                            <button type="button" class="btn-primary btn-sm" onclick="responderConexao(${c.id}, 'aceitar')">✓ Aceitar</button>
                            <button type="button" class="btn-danger btn-sm" onclick="responderConexao(${c.id}, 'recusar')">✕ Recusar</button>
                            <button type="button" class="btn-secondary btn-sm" onclick="abrirModalMapaPorConexaoId(${c.id})">🗺️ Mapa</button>
                        </div>
                    </div>
                `).join('');
            }
        }

        // 2. Conexões Ativas (Match social confirmado - chat liberado)
        if (listaAtivas) {
            if (ativas.length === 0) {
                listaAtivas.innerHTML = '<p class="empty-state">Nenhuma conexão ativa ainda. Conecte-se com membros no Feed!</p>';
            } else {
                listaAtivas.innerHTML = ativas.map(c => {
                    const isAutor = c.autorId === currentUser.id;
                    const outroNome = isAutor ? c.interessadoNome : c.autorNome;
                    const outroPapel = isAutor ? 'Interessado(a) na Rota' : 'Autor(a) da Rota';
                    return `
                        <div class="conexao-card">
                            <div>
                                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 0.5rem;">
                                    <strong style="font-size: 1.1rem; color: var(--text-primary);">${isAutor ? '👤' : '🚗'} ${escapeHtml(outroNome || 'Usuário')}</strong>
                                    <span class="badge-conectado">Conectado</span>
                                </div>
                                <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 0.5rem;">
                                    Trajeto: <strong>${escapeHtml(c.rotaOrigem || '')} ➔ ${escapeHtml(c.rotaDestino || '')}</strong>
                                </p>
                                <small style="color: var(--text-secondary); font-size: 0.75rem;">Papel na conexão: ${outroPapel}</small>
                            </div>
                            <div class="conexao-actions">
                                <a href="#chat/${c.id}" class="btn-primary btn-sm" style="text-decoration: none; text-align: center; display: inline-block;">Abrir Bate-Papo 💬</a>
                                <button type="button" class="btn-secondary btn-sm" onclick="abrirModalMapaPorConexaoId(${c.id})">🗺️ Ver Mapa</button>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        }

        // 3. Minhas Solicitações Enviadas
        if (listaEnviadas) {
            if (enviadas.length === 0) {
                listaEnviadas.innerHTML = '<p class="empty-state">Você não possui solicitações pendentes enviadas.</p>';
            } else {
                listaEnviadas.innerHTML = enviadas.map(c => {
                    const statusBadge = c.status === 'RECUSADO' 
                        ? '<span class="badge-recusado">Recusado</span>' 
                        : '<span class="badge-solicitado">Aguardando Autor</span>';
                    return `
                        <div class="conexao-card">
                            <div>
                                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 0.5rem;">
                                    <strong style="font-size: 1.1rem; color: var(--text-primary);">🚗 ${escapeHtml(c.autorNome || 'Condutor')}</strong>
                                    ${statusBadge}
                                </div>
                                <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 0.5rem;">
                                    Rota: <strong>${escapeHtml(c.rotaOrigem || '')} ➔ ${escapeHtml(c.rotaDestino || '')}</strong>
                                </p>
                                <small style="color: var(--text-secondary); font-size: 0.75rem;">📅 Enviado em: ${formatarData(c.dataSolicitacao)}</small>
                            </div>
                            <div class="conexao-actions">
                                <button type="button" class="btn-secondary btn-sm" onclick="abrirModalMapaPorConexaoId(${c.id})">🗺️ Ver Mapa</button>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        }
    } catch(err) {
        console.error('Erro ao carregar conexões:', err);
    }
}

async function responderConexao(conexaoId, acao) {
    const endpoint = acao === 'aceitar' ? 'aceitar' : 'recusar';
    try {
        const res = await fetch(`/api/conexoes/${conexaoId}/${endpoint}?autorId=${currentUser.id}`, {
            method: 'PATCH'
        });
        if (res.ok) {
            alert(acao === 'aceitar' ? 'Conexão aceita! O chat foi liberado.' : 'Solicitação recusada.');
            await carregarConexoes();
            atualizarBadgeNotificacoes();
        } else {
            const msg = await res.text();
            alert('Atenção: ' + msg);
        }
    } catch(err) {
        console.error(err);
        alert('Erro ao responder conexão.');
    }
}

function renderChat(container, conexaoId) {
    const template = document.getElementById('tpl-chat').content.cloneNode(true);
    container.appendChild(template);
    
    const messagesDiv = document.getElementById('chat-messages');
    
    // Carrega metadados da conexão para habilitar o botão "Ver Mapa do Trajeto" no cabeçalho do chat
    configurarHeaderChatMapa(Number(conexaoId));

    // Carregar mensagens reais via API
    carregarMensagensChat(conexaoId, messagesDiv);

    // Polling a cada 3 segundos enquanto estiver no chat
    currentChatInterval = setInterval(() => {
        carregarMensagensChat(conexaoId, messagesDiv, true);
    }, 3000);

    document.getElementById('form-chat').addEventListener('submit', async (e) => {
        e.preventDefault();
        const input = document.getElementById('chat-input');
        const conteudoTexto = input.value.trim();
        if (!conteudoTexto) return;

        try {
            const res = await fetch(`/api/conexoes/${conexaoId}/mensagens?remetenteId=${currentUser.id}`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({ conteudoTexto })
            });
            if (res.ok) {
                input.value = '';
                carregarMensagensChat(conexaoId, messagesDiv);
            } else {
                const msg = await res.text();
                alert('Erro ao enviar mensagem: ' + msg);
            }
        } catch(err) {
            console.error(err);
        }
    });
}

async function configurarHeaderChatMapa(conexaoId) {
    try {
        if (cachedConexoes.length === 0 && currentUser) {
            const res = await fetch(`/api/conexoes?usuarioId=${currentUser.id}`);
            if (res.ok) cachedConexoes = await res.json();
        }
        const conexao = cachedConexoes.find(c => c.id === conexaoId);
        if (!conexao) return;

        const sub = document.getElementById('chat-header-subtitle');
        const btnMapa = document.getElementById('btn-chat-mapa');
        if (sub) {
            sub.innerText = `Trajeto: ${conexao.rotaOrigem || 'Origem'} ➔ ${conexao.rotaDestino || 'Campus'}`;
        }
        if (btnMapa) {
            btnMapa.style.display = 'inline-block';
            btnMapa.onclick = () => abrirModalMapaPorConexaoId(conexaoId);
        }
    } catch (e) {
        // Silencia erro
    }
}

async function carregarMensagensChat(conexaoId, messagesDiv, isPolling = false) {
    try {
        const res = await fetch(`/api/conexoes/${conexaoId}/mensagens?usuarioId=${currentUser.id}`);
        if (res.ok) {
            const msgs = await res.json();
            
            // Se for polling e o número de mensagens reais for o mesmo, evita re-render desnecessário
            const prevCount = messagesDiv.dataset.msgCount !== undefined ? Number(messagesDiv.dataset.msgCount) : -1;
            if (isPolling && prevCount === msgs.length) {
                return;
            }
            messagesDiv.dataset.msgCount = String(msgs.length);

            messagesDiv.innerHTML = '';
            if (msgs.length === 0) {
                messagesDiv.innerHTML = '<p class="empty-state">Nenhuma mensagem ainda. Combine seu trajeto aqui!</p>';
                return;
            }

            msgs.forEach(m => {
                const isMe = m.remetenteId === currentUser.id;
                messagesDiv.innerHTML += `
                    <div style="margin-bottom: 1rem; text-align: ${isMe ? 'right' : 'left'};">
                        <div style="display: inline-block; padding: 0.75rem 1rem; border-radius: 12px; background: ${isMe ? 'var(--primary)' : 'var(--surface-border)'}; color: #fff; max-width: 80%; text-align: left;">
                            ${escapeHtml(m.conteudoTexto)}
                            <div style="font-size: 0.65rem; color: rgba(255,255,255,0.7); margin-top: 4px; text-align: right;">
                                ${formatarData(m.dataEnvio)}
                            </div>
                        </div>
                    </div>
                `;
            });
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        } else if (!isPolling) {
            const errTxt = await res.text();
            messagesDiv.innerHTML = `<p class="empty-state">${escapeHtml(errTxt || 'Não foi possível carregar as mensagens desta conexão.')}</p>`;
        }
    } catch(err) {
        if (!isPolling) {
            messagesDiv.innerHTML = '<p class="empty-state">Erro ao carregar mensagens. A conexão pode não existir ou não estar no estado CONECTADO.</p>';
        }
    }
}

// ============================================================================
// MOTOR DE MAPA INTERATIVO 100% GRATUITO (Leaflet + OpenStreetMap + OSRM + GPS)
// ============================================================================

const CAMPI_IFSP_CONHECIDOS = [
    { chave: 'birigui', nome: 'IFSP Campus Birigui', lat: -21.2673, lng: -50.3204, cidade: 'Birigui, SP' },
    { chave: 'votuporanga', nome: 'IFSP Campus Votuporanga', lat: -20.4034, lng: -49.9678, cidade: 'Votuporanga, SP' },
    { chave: 'sao paulo', nome: 'IFSP Campus São Paulo', lat: -23.5249, lng: -46.6231, cidade: 'São Paulo, SP' },
    { chave: 'sao carlos', nome: 'IFSP Campus São Carlos', lat: -21.9818, lng: -47.8819, cidade: 'São Carlos, SP' },
    { chave: 'campinas', nome: 'IFSP Campus Campinas', lat: -22.8542, lng: -47.1292, cidade: 'Campinas, SP' },
    { chave: 'araraquara', nome: 'IFSP Campus Araraquara', lat: -21.8126, lng: -48.1966, cidade: 'Araraquara, SP' },
    { chave: 'rio preto', nome: 'IFSP Campus São José do Rio Preto', lat: -20.7850, lng: -49.3597, cidade: 'São José do Rio Preto, SP' },
    { chave: 'catanduva', nome: 'IFSP Campus Catanduva', lat: -21.1558, lng: -48.9678, cidade: 'Catanduva, SP' },
    { chave: 'barretos', nome: 'IFSP Campus Barretos', lat: -20.5694, lng: -48.5586, cidade: 'Barretos, SP' },
    { chave: 'epitacio', nome: 'IFSP Campus Presidente Epitácio', lat: -21.7731, lng: -52.1068, cidade: 'Presidente Epitácio, SP' }
];

function normalizarTexto(str) {
    return (str || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

function criarIconePin(corHex, emoji) {
    if (typeof L === 'undefined') return null;
    return L.divIcon({
        className: 'custom-leaflet-pin',
        html: `<div class="ifrotas-pin" style="background: ${corHex};"><span>${emoji}</span></div>`,
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -32]
    });
}

async function resolverCoordenadasCampus(textoDestino) {
    const norm = normalizarTexto(textoDestino);
    for (const c of CAMPI_IFSP_CONHECIDOS) {
        if (norm.includes(c.chave)) {
            return { lat: c.lat, lng: c.lng, nome: c.nome, cidade: c.cidade };
        }
    }
    const geo = await geocodificarEnderecoLivre(textoDestino + ', São Paulo, Brasil');
    if (geo) {
        return { lat: geo.lat, lng: geo.lng, nome: textoDestino, cidade: 'SP' };
    }
    // Fallback padrão: IFSP Campus Birigui
    return { lat: -21.2673, lng: -50.3204, nome: textoDestino || 'IFSP Campus Birigui', cidade: 'Birigui, SP' };
}

async function geocodificarEnderecoLivre(consulta) {
    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(consulta)}&limit=1`;
        const res = await fetch(url, { headers: { 'Accept-Language': 'pt-BR' } });
        if (!res.ok) return null;
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) {
            return {
                lat: parseFloat(data[0].lat),
                lng: parseFloat(data[0].lon),
                displayName: data[0].display_name
            };
        }
    } catch (e) {
        console.warn('Geocoding falhou:', e);
    }
    return null;
}

async function geocodificarReversoBairro(lat, lng) {
    try {
        const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;
        const res = await fetch(url, { headers: { 'Accept-Language': 'pt-BR' } });
        if (!res.ok) return null;
        const data = await res.json();
        if (data && data.address) {
            const addr = data.address;
            const bairro = addr.suburb || addr.neighbourhood || addr.city_district || addr.road || addr.residential;
            const cidade = addr.city || addr.town || addr.municipality;
            if (bairro && cidade) return `${bairro}, ${cidade}`;
            if (bairro) return bairro;
            if (cidade) return cidade;
        }
    } catch (e) {
        console.warn('Reverse geocoding falhou:', e);
    }
    return `Lat ${lat.toFixed(4)}, Lng ${lng.toFixed(4)}`;
}

function calcularDistanciaHaversineKm(lat1, lon1, lat2, lon2) {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) ** 2 +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

async function calcularRotaOSRM(lat1, lng1, lat2, lng2) {
    try {
        const url = `https://router.project-osrm.org/route/v1/driving/${lng1},${lat1};${lng2},${lat2}?overview=full&geometries=geojson`;
        const res = await fetch(url);
        if (res.ok) {
            const data = await res.json();
            if (data.routes && data.routes.length > 0) {
                const route = data.routes[0];
                const coordsLatLng = route.geometry.coordinates.map(c => [c[1], c[0]]);
                const distKm = (route.distance / 1000).toFixed(1) + ' km';
                const durMin = Math.max(1, Math.round(route.duration / 60)) + ' min';
                return { coords: coordsLatLng, distancia: distKm, duracao: durMin, isRealRoad: true };
            }
        }
    } catch (e) {
        console.warn('OSRM offline, usando linha direta estimada.', e);
    }
    // Fallback em linha reta caso o servidor público OSRM esteja indisponível
    const dist = calcularDistanciaHaversineKm(lat1, lng1, lat2, lng2);
    const distKm = (dist * 1.25).toFixed(1) + ' km';
    const durMin = Math.max(2, Math.round((dist * 1.25 / 35) * 60)) + ' min';
    return {
        coords: [[lat1, lng1], [lat2, lng2]],
        distancia: distKm,
        duracao: durMin,
        isRealRoad: false
    };
}

// ----------------------------------------------------------------------------
// 1. Mapa Interativo na Tela de Publicar Nova Rota (#rotas/nova)
// ----------------------------------------------------------------------------
async function inicializarMapaNovaRota() {
    const mapEl = document.getElementById('mapa-nova-rota');
    if (!mapEl || typeof L === 'undefined') return;

    // Reseta estado anterior
    if (novaRotaMapState.map) {
        novaRotaMapState.map.remove();
    }

    const campusInicial = CAMPI_IFSP_CONHECIDOS[0]; // IFSP Birigui
    novaRotaMapState = {
        map: null,
        markerOrigem: null,
        markerDestino: null,
        routeLayer: null,
        latOrigem: null,
        lngOrigem: null,
        latDestino: campusInicial.lat,
        lngDestino: campusInicial.lng,
        distanciaEstimada: null,
        duracaoEstimada: null
    };

    const map = L.map(mapEl).setView([campusInicial.lat, campusInicial.lng], 13);
    novaRotaMapState.map = map;

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    // Marcador do Campus
    novaRotaMapState.markerDestino = L.marker([campusInicial.lat, campusInicial.lng], {
        icon: criarIconePin('#ef4444', '🏫')
    }).addTo(map).bindPopup(`<strong>Destino:</strong> ${escapeHtml(campusInicial.nome)}`);

    // Clique no mapa define a origem e calcula a rota
    map.on('click', async (e) => {
        const { lat, lng } = e.latlng;
        await definirOrigemNoMapaNovaRota(lat, lng, true);
    });

    // Botão: Usar GPS atual
    const btnGps = document.getElementById('btn-usar-gps');
    if (btnGps) {
        btnGps.addEventListener('click', () => {
            if (!navigator.geolocation) {
                alert('Geolocalização não suportada pelo seu navegador.');
                return;
            }
            btnGps.innerText = '⏳ Obtendo localização GPS...';
            navigator.geolocation.getCurrentPosition(
                async (pos) => {
                    btnGps.innerText = '📍 Localização GPS Aplicada!';
                    await definirOrigemNoMapaNovaRota(pos.coords.latitude, pos.coords.longitude, true);
                },
                () => {
                    btnGps.innerText = '📍 Usar Minha Localização Atual (GPS)';
                    alert('Não foi possível acessar o GPS. Você também pode clicar diretamente em qualquer ponto do mapa ou buscar o bairro!');
                },
                { enableHighAccuracy: true, timeout: 8000 }
            );
        });
    }

    // Botão: Buscar endereço de origem digitado
    const btnBuscarOrigem = document.getElementById('btn-buscar-origem');
    if (btnBuscarOrigem) {
        btnBuscarOrigem.addEventListener('click', async () => {
            const bairroInput = document.getElementById('rota-bairro');
            const destinoInput = document.getElementById('rota-destino');
            if (!bairroInput || !bairroInput.value.trim()) {
                alert('Digite o nome do bairro ou rua antes de buscar.');
                return;
            }
            const infoCampus = await resolverCoordenadasCampus(destinoInput ? destinoInput.value : '');
            const geo = await geocodificarEnderecoLivre(`${bairroInput.value.trim()}, ${infoCampus.cidade}, Brasil`);
            if (geo) {
                await definirOrigemNoMapaNovaRota(geo.lat, geo.lng, false);
            } else {
                alert('Endereço não encontrado automaticamente. Clique diretamente no mapa para marcar o ponto de saída!');
            }
        });
    }

    // Botão ou mudança de Destino (Campus)
    const btnBuscarDestino = document.getElementById('btn-buscar-destino');
    const inputDestino = document.getElementById('rota-destino');
    const atualizarCampusDestino = async () => {
        if (!inputDestino || !inputDestino.value.trim()) return;
        const info = await resolverCoordenadasCampus(inputDestino.value.trim());
        novaRotaMapState.latDestino = info.lat;
        novaRotaMapState.lngDestino = info.lng;

        if (novaRotaMapState.markerDestino) {
            novaRotaMapState.markerDestino.setLatLng([info.lat, info.lng])
                .bindPopup(`<strong>Destino:</strong> ${escapeHtml(inputDestino.value.trim())}`).openPopup();
        }
        if (novaRotaMapState.latOrigem != null && novaRotaMapState.lngOrigem != null) {
            await desenharTrajetoNovaRota();
        } else {
            map.setView([info.lat, info.lng], 13);
        }
    };

    if (btnBuscarDestino) btnBuscarDestino.addEventListener('click', atualizarCampusDestino);
    if (inputDestino) inputDestino.addEventListener('change', atualizarCampusDestino);
}

async function definirOrigemNoMapaNovaRota(lat, lng, preencherBairroReverso = true) {
    const map = novaRotaMapState.map;
    if (!map) return;

    novaRotaMapState.latOrigem = lat;
    novaRotaMapState.lngOrigem = lng;

    if (!novaRotaMapState.markerOrigem) {
        novaRotaMapState.markerOrigem = L.marker([lat, lng], {
            icon: criarIconePin('#10b981', '🚗')
        }).addTo(map);
    } else {
        novaRotaMapState.markerOrigem.setLatLng([lat, lng]);
    }

    if (preencherBairroReverso) {
        const inputBairro = document.getElementById('rota-bairro');
        if (inputBairro) {
            const nomeBairro = await geocodificarReversoBairro(lat, lng);
            if (nomeBairro) inputBairro.value = nomeBairro;
        }
    }

    const labelOrigem = document.getElementById('rota-bairro')?.value || 'Ponto de Partida';
    novaRotaMapState.markerOrigem.bindPopup(`<strong>Saída:</strong> ${escapeHtml(labelOrigem)}`).openPopup();

    await desenharTrajetoNovaRota();
}

async function desenharTrajetoNovaRota() {
    const { map, latOrigem, lngOrigem, latDestino, lngDestino } = novaRotaMapState;
    if (!map || latOrigem == null || latDestino == null) return;

    const resumoEl = document.getElementById('nova-rota-resumo');
    if (resumoEl) {
        resumoEl.innerHTML = '<span>⏳ Calculando melhor caminho até o campus...</span>';
    }

    const resultado = await calcularRotaOSRM(latOrigem, lngOrigem, latDestino, lngDestino);
    novaRotaMapState.distanciaEstimada = resultado.distancia;
    novaRotaMapState.duracaoEstimada = resultado.duracao;

    if (novaRotaMapState.routeLayer) {
        map.removeLayer(novaRotaMapState.routeLayer);
    }

    novaRotaMapState.routeLayer = L.polyline(resultado.coords, {
        color: '#10b981',
        weight: 5,
        opacity: 0.9,
        dashArray: resultado.isRealRoad ? null : '8, 8'
    }).addTo(map);

    map.fitBounds(novaRotaMapState.routeLayer.getBounds(), { padding: [35, 35] });

    if (resumoEl) {
        resumoEl.innerHTML = `
            <div class="map-metrics">
                <span>🛣️ <strong>Distância:</strong> ${escapeHtml(resultado.distancia)}</span>
                <span>⏱️ <strong>Tempo estimado:</strong> ~${escapeHtml(resultado.duracao)}</span>
            </div>
            <span class="badge-conectado">Trajeto Pronto</span>
        `;
    }
}

// ----------------------------------------------------------------------------
// 2. Modal de Visualização de Mapa (Feed, Conexões e Chat)
// ----------------------------------------------------------------------------
async function abrirModalMapaPorRotaId(rotaId) {
    const rota = cachedRotas.find(r => r.id === rotaId);
    if (!rota) return;

    await abrirModalMapa({
        titulo: `${rota.bairroOrigem} ➔ ${rota.destinoCampus}`,
        subtitulo: `Publicada por ${rota.autor?.nome || 'Membro IFSP'} • Horário: ${rota.horarioHabitual || ''}`,
        bairroOrigem: rota.bairroOrigem,
        destinoCampus: rota.destinoCampus,
        latOrigem: rota.latitudeOrigem,
        lngOrigem: rota.longitudeOrigem,
        latDestino: rota.latitudeDestino,
        lngDestino: rota.longitudeDestino,
        distanciaEstimada: rota.distanciaEstimada,
        duracaoEstimada: rota.duracaoEstimada
    });
}

async function abrirModalMapaPorConexaoId(conexaoId) {
    const c = cachedConexoes.find(item => item.id === conexaoId);
    if (!c) return;

    await abrirModalMapa({
        titulo: `${c.rotaOrigem || 'Origem'} ➔ ${c.rotaDestino || 'Campus'}`,
        subtitulo: `Condutor(a): ${c.autorNome || ''} • Interessado(a): ${c.interessadoNome || ''}`,
        bairroOrigem: c.rotaOrigem,
        destinoCampus: c.rotaDestino,
        latOrigem: c.latitudeOrigem,
        lngOrigem: c.longitudeOrigem,
        latDestino: c.latitudeDestino,
        lngDestino: c.longitudeDestino,
        distanciaEstimada: c.distanciaEstimada,
        duracaoEstimada: c.duracaoEstimada
    });
}

async function abrirModalMapa(dados) {
    const dialog = document.getElementById('modal-mapa');
    if (!dialog || typeof L === 'undefined') return;

    modalMapState.currentDados = dados;

    document.getElementById('modal-mapa-title').innerText = `🗺️ ${dados.titulo}`;
    document.getElementById('modal-mapa-subtitle').innerText = dados.subtitulo || '';
    document.getElementById('modal-mapa-metricas').innerHTML = '<span>⏳ Traçando itinerário no mapa...</span>';

    // Abre usando API nativa showModal() conforme Modern Web Guidance
    if (!dialog.open) {
        dialog.showModal();
    }

    const canvas = document.getElementById('mapa-modal-canvas');
    if (modalMapState.map) {
        modalMapState.map.remove();
        modalMapState.map = null;
    }

    // Resolve coordenadas do Campus (Destino) e contexto de cidade
    let latDest = dados.latDestino;
    let lngDest = dados.lngDestino;
    const infoCampus = await resolverCoordenadasCampus(dados.destinoCampus);
    const cidadeContexto = infoCampus.cidade || 'Birigui, SP';

    if (latDest == null || lngDest == null) {
        latDest = infoCampus.lat;
        lngDest = infoCampus.lng;
    }

    // Resolve coordenadas da Origem (Bairro)
    let latOrig = dados.latOrigem;
    let lngOrig = dados.lngOrigem;
    if (latOrig == null || lngOrig == null) {
        const geoOrig = await geocodificarEnderecoLivre(`${dados.bairroOrigem}, ${cidadeContexto}, Brasil`);
        if (geoOrig) {
            latOrig = geoOrig.lat;
            lngOrig = geoOrig.lng;
        } else {
            // Deslocamento aproximado de ~2.5km do campus caso o bairro seja genérico
            latOrig = latDest - 0.018;
            lngOrig = lngDest - 0.015;
        }
    }

    modalMapState.currentDados.latOrigem = latOrig;
    modalMapState.currentDados.lngOrigem = lngOrig;
    modalMapState.currentDados.latDestino = latDest;
    modalMapState.currentDados.lngDestino = lngDest;

    const map = L.map(canvas).setView([latDest, lngDest], 13);
    modalMapState.map = map;

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    modalMapState.markerOrigem = L.marker([latOrig, lngOrig], {
        icon: criarIconePin('#10b981', '🚗')
    }).addTo(map).bindPopup(`<strong>Origem:</strong> ${escapeHtml(dados.bairroOrigem)}`);

    modalMapState.markerDestino = L.marker([latDest, lngDest], {
        icon: criarIconePin('#ef4444', '🏫')
    }).addTo(map).bindPopup(`<strong>Destino:</strong> ${escapeHtml(dados.destinoCampus)}`);

    const rotaCalculada = await calcularRotaOSRM(latOrig, lngOrig, latDest, lngDest);

    modalMapState.routeLayer = L.polyline(rotaCalculada.coords, {
        color: '#10b981',
        weight: 5,
        opacity: 0.9,
        dashArray: rotaCalculada.isRealRoad ? null : '8, 8'
    }).addTo(map);

    setTimeout(() => {
        map.invalidateSize();
        map.fitBounds(modalMapState.routeLayer.getBounds(), { padding: [40, 40] });
    }, 100);

    const distTexto = dados.distanciaEstimada || rotaCalculada.distancia;
    const durTexto = dados.duracaoEstimada || rotaCalculada.duracao;

    document.getElementById('modal-mapa-metricas').innerHTML = `
        <span>🛣️ <strong>Distância:</strong> ${escapeHtml(distTexto)}</span>
        <span>⏱️ <strong>Tempo até o campus:</strong> ~${escapeHtml(durTexto)}</span>
    `;
}

function mostrarMinhaLocalizacaoNoModal() {
    if (!modalMapState.map) return;
    if (!navigator.geolocation) {
        alert('Seu navegador não suporta geolocalização.');
        return;
    }

    const btn = document.getElementById('btn-modal-minha-loc');
    if (btn) btn.innerText = '⏳ Localizando GPS...';

    navigator.geolocation.getCurrentPosition(
        (pos) => {
            if (btn) btn.innerText = '📍 Você está no mapa!';
            const userLat = pos.coords.latitude;
            const userLng = pos.coords.longitude;
            const map = modalMapState.map;

            if (modalMapState.markerUser) {
                map.removeLayer(modalMapState.markerUser);
            }

            modalMapState.markerUser = L.marker([userLat, userLng], {
                icon: criarIconePin('#3b82f6', '📍')
            }).addTo(map).bindPopup('<strong>Sua Localização Atual (GPS)</strong>').openPopup();

            const d = modalMapState.currentDados;
            if (d && d.latOrigem != null) {
                const distAteSaida = calcularDistanciaHaversineKm(userLat, userLng, d.latOrigem, d.lngOrigem).toFixed(1);
                const metricasEl = document.getElementById('modal-mapa-metricas');
                if (metricasEl && !metricasEl.innerHTML.includes('Distância até a saída')) {
                    metricasEl.innerHTML += `<span style="color: var(--accent);">📍 <strong>Distância até a saída:</strong> ~${distAteSaida} km</span>`;
                }
            }

            const bounds = L.latLngBounds([
                [userLat, userLng],
                [d.latOrigem, d.lngOrigem],
                [d.latDestino, d.lngDestino]
            ]);
            map.fitBounds(bounds, { padding: [40, 40] });
        },
        () => {
            if (btn) btn.innerText = '📍 Mostrar Minha Localização';
            alert('Permissão de GPS negada ou indisponível no navegador.');
        },
        { enableHighAccuracy: true, timeout: 8000 }
    );
}
