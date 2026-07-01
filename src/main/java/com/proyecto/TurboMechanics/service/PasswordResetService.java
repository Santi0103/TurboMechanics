package com.proyecto.TurboMechanics.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.ForgotPasswordRequestDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.ResetPasswordRequestDTO;
import com.proyecto.TurboMechanics.dto.ValidateCodeRequestDTO;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /** Minutos de validez del código */
    private static final int CODE_EXPIRY_MINUTES = 15;

    /**
     * Paso 1: recibe correo o teléfono, busca el usuario, genera un código de 6 dígitos,
     * lo guarda con su expiración y lo envía por correo electrónico.
     * Si el dato no corresponde a ningún usuario registrado, lanza excepción.
     *
     * @param request DTO con emailOrPhone
     * @return mensaje de confirmación de envío
     */
    @Transactional
    public MessageResponseDTO sendResetCode(ForgotPasswordRequestDTO request) {

        Users user = findUserByEmailOrPhone(request.getEmailOrPhone());

        String code = generateCode();
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        usersRepository.save(user);

        // Llamada asíncrona: no bloquea, el método sigue y responde de una
        emailService.sendResetCodeEmail(user.getEmail(), code, CODE_EXPIRY_MINUTES);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Código de recuperación enviado al correo registrado.");
        return response;
    }

    /**
     * Paso 2 (opcional): valida que el código ingresado sea correcto y no haya expirado,
     * sin cambiar la contraseña todavía. Útil para pantallas de confirmación de código.
     *
     * @param request DTO con emailOrPhone y code
     * @return mensaje de validación exitosa
     */
    @Transactional(readOnly = true)
    public MessageResponseDTO validateCode(ValidateCodeRequestDTO request) {

        Users user = findUserByEmailOrPhone(request.getEmailOrPhone());
        verifyCode(user, request.getCode());

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Código válido. Puedes restablecer tu contraseña.");
        return response;
    }

    /**
     * Paso 3: valida el código y, si es correcto, actualiza la contraseña del usuario
     * con el nuevo valor proporcionado. Luego borra el código para que no pueda reutilizarse.
     *
     * @param request DTO con emailOrPhone, code y newPassword
     * @return mensaje de éxito
     */
    @Transactional
    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO request) {

        Users user = findUserByEmailOrPhone(request.getEmailOrPhone());
        verifyCode(user, request.getCode());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        // Invalida el código para que no pueda reutilizarse
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        usersRepository.save(user);

        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessage("Contraseña restablecida exitosamente.");
        return response;
    }

    /**
     * Busca un usuario por correo electrónico o por número de teléfono/WhatsApp.
     * Si no se encuentra por ninguno de los dos, lanza una excepción
     *
     * @param emailOrPhone correo o número ingresado por el usuario
     * @return el usuario encontrado
     */
    private Users findUserByEmailOrPhone(String emailOrPhone) {
        Optional<Users> byEmail = usersRepository.findByEmail(emailOrPhone);
        if (byEmail.isPresent()) return byEmail.get();

        Optional<Users> byPhone = usersRepository.findByPhone(emailOrPhone);
        if (byPhone.isPresent()) return byPhone.get();

        throw new RuntimeException("Usuario o contraseña inválidos.");
    }

    /**
     * Verifica que el código sea correcto y que no haya expirado.
     *
     * @param user el usuario al que pertenece el código
     * @param code el código ingresado por el usuario
     */
    private void verifyCode(Users user, String code) {
        if (user.getResetCode() == null || user.getResetCodeExpiry() == null) {
            throw new RuntimeException("No hay un código de recuperación activo. Solicita uno nuevo.");
        }
        if (LocalDateTime.now().isAfter(user.getResetCodeExpiry())) {
            throw new RuntimeException("El código de recuperación ha expirado. Solicita uno nuevo.");
        }
        if (!user.getResetCode().equals(code)) {
            throw new RuntimeException("El código de recuperación es incorrecto.");
        }
    }

    /**
     * Genera un código numérico de 6 dígitos usando SecureRandom.
     *
     * @return código como String de 6 caracteres
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}