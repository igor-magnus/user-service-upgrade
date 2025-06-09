package com.example.ui;

import com.example.dao.UserDAOImpl;
import com.example.model.User;
import com.example.service.UserService;
import com.example.config.HibernateUtil;
import com.example.service.UserServiceException;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUserInterface {

    private final UserService userService;
    private final Scanner scanner;

    public ConsoleUserInterface() {
        this.userService = new UserService(new UserDAOImpl());
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createUser();
                    break;
                case "2":
                    getUserById();
                    break;
                case "3":
                    getAllUsers();
                    break;
                case "4":
                    updateUser();
                    break;
                case "5":
                    deleteUser();
                    break;
                case "6":
                    running = false;
                    System.out.println("Выход из приложения.");
                    break;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте еще раз.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\nМеню:");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Получить пользователя по ID");
        System.out.println("3. Получить всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Выйти");
    }

    private void createUser() {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine();
        System.out.print("Введите email: ");
        String email = scanner.nextLine();
        System.out.print("Введите возраст: ");
        Integer age = null;
        try {
            age = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Некорректный формат возраста.  Пожалуйста, введите число.");
            return; // Выходим из метода, чтобы избежать дальнейших ошибок
        }

        try {
            User createdUser = userService.createUser(name, email, age);
            System.out.println("Пользователь создан: " + createdUser);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка при создании пользователя: " + e.getMessage()); // Выводим сообщение валидации
        } catch (UserServiceException e) {
            System.err.println("Ошибка при создании пользователя: Произошла ошибка. Попробуйте позже."); // Общее сообщение
        }
    }

    private void getUserById() {
        System.out.print("Введите ID пользователя: ");
        Long id = null;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Некорректный формат ID. Пожалуйста, введите число.");
            return; // Выходим из метода
        }

        try {
            Optional<User> user = userService.getUserById(id);
            Long finalId = id;
            user.ifPresentOrElse(
                    System.out::println,
                    () -> System.out.println("Пользователь с ID " + finalId + " не найден.")
            );
        }  catch (UserServiceException e) {
            System.err.println("Ошибка при получении пользователя по ID: Произошла ошибка. Попробуйте позже.");
        }
    }

    private void getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("Список пользователей пуст.");
            } else {
                users.forEach(System.out::println);
            }
        }  catch (UserServiceException e) {
            System.err.println("Ошибка при получении всех пользователей: Произошла ошибка. Попробуйте позже.");
        }
    }

    private void updateUser() {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = null;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Некорректный формат ID. Пожалуйста, введите число.");
            return;
        }

        Optional<User> existingUser = userService.getUserById(id);
        if (existingUser.isPresent()) {
            System.out.print("Введите новое имя (" + existingUser.get().getName() + "): ");
            String name = scanner.nextLine();
            if (name.isEmpty()) name = existingUser.get().getName();

            System.out.print("Введите новый email (" + existingUser.get().getEmail() + "): ");
            String email = scanner.nextLine();
            if (email.isEmpty()) email = existingUser.get().getEmail();

            System.out.print("Введите новый возраст (" + existingUser.get().getAge() + "): ");
            String ageString = scanner.nextLine();
            Integer age = (ageString.isEmpty()) ? existingUser.get().getAge() : null;
            try {
                if (!ageString.isEmpty()) {
                    age = Integer.parseInt(ageString);
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка: Некорректный формат возраста. Пожалуйста, введите число.");
                return;
            }


            try {
                Optional<User> updatedUser = userService.updateUser(id, name, email, age);
                updatedUser.ifPresentOrElse(
                        user -> System.out.println("Пользователь обновлен: " + user),
                        () -> System.out.println("Не удалось обновить пользователя.")
                );
            }  catch (IllegalArgumentException e) {
                System.err.println("Ошибка при обновлении пользователя: " + e.getMessage());
            }  catch (UserServiceException e) {
                System.err.println("Ошибка при обновлении пользователя: Произошла ошибка. Попробуйте позже.");
            }
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    private void deleteUser() {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = null;
        try {
            id = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Некорректный формат ID. Пожалуйста, введите число.");
            return;
        }


        try {
            if (userService.deleteUser(id)) {
                System.out.println("Пользователь с ID " + id + " удален.");
            } else {
                System.out.println("Пользователь с ID " + id + " не найден.");
            }
        }  catch (UserServiceException e) {
            System.err.println("Ошибка при удалении пользователя: Произошла ошибка. Попробуйте позже.");
        }
    }

    public static void main(String[] args) {
        ConsoleUserInterface ui = new ConsoleUserInterface();
        ui.run();
        HibernateUtil.shutdown();
    }
}