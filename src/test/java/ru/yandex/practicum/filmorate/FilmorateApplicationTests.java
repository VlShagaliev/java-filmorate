package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private FilmController filmController;
    @Autowired
    private UserController userController;

    @BeforeEach
    public void clear(){
        userController.users().clear();
        filmController.films().clear();
    }

    @Test
    public void testAddFilm_Success() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film result = filmController.addFilm(film);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Фильм", result.getName());
        assertEquals(1, filmController.films().size());
    }

    @Test
    public void testAddFilm_EmptyName_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    public void testAddFilm_DescriptionTooLong_ShouldThrowValidationException() {
        String longDescription = "a".repeat(201);
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription(longDescription);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    public void testAddFilm_ReleaseDateBefore1895_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    public void testAddFilm_NonPositiveDuration_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.addFilm(film));
    }

    @Test
    public void testUpdate_Success() {
        // Сначала добавим фильм
        Film original = new Film();
        original.setName("Фильм");
        original.setDescription("Описание");
        original.setReleaseDate(LocalDate.of(2000, 1, 1));
        original.setDuration(100);

        filmController.addFilm(original);

        // Обновляем фильм
        Film updated = new Film();
        updated.setId(1);
        updated.setName("Обновленный фильм");
        updated.setDescription("Обновленное описание");
        updated.setReleaseDate(LocalDate.of(2010, 5, 5));
        updated.setDuration(150);

        Film result = filmController.update(updated);

        assertEquals("Обновленный фильм", result.getName());
    }

    @Test
    public void testUpdate_FilmNotExist_ShouldThrowNotFoundException() {
        Film nonExistent = new Film();
        nonExistent.setId(999);
        nonExistent.setName("Фильм");
        nonExistent.setDescription("Описание");
        nonExistent.setReleaseDate(LocalDate.of(2000, 1, 1));
        nonExistent.setDuration(100);

        Exception exception = assertThrows(NotFoundException.class,
                () -> filmController.update(nonExistent));

        assertEquals("Фильм отсутствует в списке", exception.getMessage());
    }

    @Test
    public void testUpdate_InvalidId_ShouldThrowValidationException() {
        Film invalidId = new Film();
        invalidId.setId(-1);
        invalidId.setName("Название");
        invalidId.setDescription("Описание");
        invalidId.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidId.setDuration(100);

        Exception exception = assertThrows(ValidationException.class,
                () -> filmController.update(invalidId));

        assertEquals("Отсутствует id фильма", exception.getMessage());
    }

    @Test
    public void testAddUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User result = userController.add(user);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("test", result.getLogin());
        assertEquals("Test Name", result.getName());
        assertEquals(LocalDate.of(1990, 1, 1), result.getBirthday());
    }

    @Test
    public void testAddUser_InvalidEmail_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("invalidEmail");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    public void testAddUser_EmailEmpty_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    public void testAddUser_LoginWithSpaces_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("Test with spaces");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    public void testAddUser_BirthdayInFuture_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    public void testAddUser_NameNull_ShouldSetLoginAsName() {
        User user = new User();

        user.setEmail("test@example.com");
        user.setLogin("test");
        // Name не установлено
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User result = userController.add(user);

        assertEquals("test", result.getName());
    }

    @Test
    public void testUserUpdate_Success() {
        // Добавим пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userController.add(user);

        // Обновляем пользователя
        User updated = new User();
        updated.setId(1);
        updated.setEmail("newemail@example.com");
        updated.setLogin("newlogin");
        updated.setName("New Name");
        updated.setBirthday(LocalDate.of(1985, 5, 5));

        User result = userController.update(updated);

        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("New Name", result.getName());
    }

    @Test
    public void testUserUpdate_UserNotExist_ShouldThrowNotFoundException() {
        User nonExistent = new User();
        nonExistent.setId(999);
        nonExistent.setEmail("email@example.com");
        nonExistent.setLogin("test");
        nonExistent.setBirthday(LocalDate.of(1990, 1, 1));

        Exception exception = assertThrows(NotFoundException.class,
                () -> userController.update(nonExistent));

        assertEquals("Пользователь с данным id отсутствует в списке", exception.getMessage());
    }

    @Test
    public void testUserUpdate_InvalidId_ShouldThrowValidationException() {
        User invalidIdUser = new User();
        invalidIdUser.setId(-5);
        invalidIdUser.setEmail("email@example.com");
        invalidIdUser.setLogin("test");
        invalidIdUser.setBirthday(LocalDate.of(1990, 1, 1));

        Exception exception = assertThrows(ValidationException.class,
                () -> userController.update(invalidIdUser));

        assertEquals("Отсутствует id пользователя", exception.getMessage());
    }


}
