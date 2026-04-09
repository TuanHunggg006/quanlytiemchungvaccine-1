package com.example.vaccinationsystem.controller;

import com.example.vaccinationsystem.dto.AccountCreateRequest;
import com.example.vaccinationsystem.dto.AccountInfoDTO;
import com.example.vaccinationsystem.service.AccountService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountInfoDTO> listAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody AccountCreateRequest request) {
        try {
            String newId = accountService.createAccount(request);
            return ResponseEntity.ok(Collections.singletonMap(
                    "message", "Tạo tài khoản thành công. Account ID: " + newId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap(
                    "message", e.getMessage()
            ));
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Collections.singletonMap(
                    "message", "Dữ liệu bị trùng hoặc vi phạm ràng buộc CSDL"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap(
                    "message", "Lỗi khi tạo tài khoản: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable String id, @RequestBody AccountCreateRequest request) {
        try {
            accountService.updateAccount(id, request);
            return ResponseEntity.ok(Collections.singletonMap("message", "Updated"));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(Collections.singletonMap("message", "Lỗi khi cập nhật vai trò: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        try {
            accountService.deleteAccount(id);
            return ResponseEntity.ok(Collections.singletonMap("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(Collections.singletonMap("message", "Không thể xóa: Nhân sự này có ràng buộc dữ liệu (tiêm chủng/thanh toán) chưa thể gỡ bỏ."));
        }
    }

    @GetMapping("/search")
    public List<AccountInfoDTO> searchStaff(
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return accountService.searchStaff(role, keyword);
    }
}
