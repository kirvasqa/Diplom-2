package praktikum.generators;

import java.util.Random;

public class UserDataGenerator {
    private static final String LATIN_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    public static String generateRandomString(String characters, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(RANDOM.nextInt(characters.length())));
        }
        return result.toString();
    }

    public static String generateRandomEmail(int length) {
        String localPart = generateRandomString(LATIN_CHARACTERS, length);
        return localPart + "@yandex.ru";
    }

    public static String generateRandomPassword(int length) {
        return generateRandomString(LATIN_CHARACTERS, length);
    }

    public static String generateRandomName(int length) {
        return generateRandomString(LATIN_CHARACTERS, length);
    }
}