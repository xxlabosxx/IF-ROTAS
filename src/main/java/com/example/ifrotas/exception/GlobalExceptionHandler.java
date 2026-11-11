package com.example.ifrotas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<String> handleRegraNegocioException(RegraNegocioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<String> handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<String> handleEstadoInvalidoException(EstadoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String erros = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + (err.getDefaultMessage() != null ? err.getDefaultMessage() : "valor inválido"))
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dados obrigatórios inválidos ou não informados (" + erros + ").");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<String> handleBadRequestExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Formato de dados ou parâmetros inválidos na requisição. Verifique os campos preenchidos.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível concluir a operação no momento. Verifique os dados informados e tente novamente.");
    }
}

