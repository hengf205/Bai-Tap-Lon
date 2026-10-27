package com.example.qlkytucxa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Converts expected application errors into safe, user-facing pages. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException exception, Model model) {
        model.addAttribute("errorTitle", "Không tìm thấy dữ liệu");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/general";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicate(DuplicateResourceException exception, Model model) {
        model.addAttribute("errorTitle", "Dữ liệu đã tồn tại");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/general";
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidOperation(InvalidOperationException exception, Model model) {
        model.addAttribute("errorTitle", "Không thể thực hiện thao tác");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error/general";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Model model) {
        model.addAttribute("errorTitle", "Không đủ quyền truy cập");
        model.addAttribute("errorMessage", "Tài khoản của bạn không được phép thực hiện thao tác này.");
        return "error/general";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpected(Model model) {
        model.addAttribute("errorTitle", "Đã xảy ra lỗi");
        model.addAttribute("errorMessage", "Hệ thống chưa thể xử lý yêu cầu. Vui lòng thử lại sau.");
        return "error/general";
    }
}
