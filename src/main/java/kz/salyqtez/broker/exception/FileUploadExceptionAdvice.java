package kz.salyqtez.broker.exception;

import kz.salyqtez.broker.message.ResponseMessage;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FileUploadExceptionAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ResponseMessage> handleMaxSizeException(MaxUploadSizeExceededException exc) {
    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("File too large!"));
  }

  @ExceptionHandler({ NotFoundException.class })
  protected ResponseEntity<Object> handleNotFound(
          Exception ex, WebRequest request) {
    return handleExceptionInternal(ex, "Book not found",
            new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  @ExceptionHandler({ IdMismatchException.class,
          ConstraintViolationException.class,
          DataIntegrityViolationException.class })
  public ResponseEntity<Object> handleBadRequest(
          Exception ex, WebRequest request) {
    return handleExceptionInternal(ex, ex.getLocalizedMessage(),
            new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }
}