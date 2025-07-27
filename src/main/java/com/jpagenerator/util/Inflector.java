package com.jpagenerator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Inflector {

    public Inflector() {
    }

    private record Rule(Pattern pattern, String replacement) {
    }

    private static final List<Rule> SINGULAR_RULES = new ArrayList<>();
    private static final Map<String, String> IRREGULAR = new LinkedHashMap<>(); // plural -> singular
    private static final Set<String> UNCOUNTABLE = new HashSet<>();

    static {
        // Incontáveis
        Collections.addAll(UNCOUNTABLE,
                "tórax", "tênis", "ônibus", "lápis", "fênix", "óculos",
                "vírus", "status", "atlas"
        );

        // Irregulares (plural -> singular)
        IRREGULAR.put("países", "país");
        IRREGULAR.put("cães", "cão");
        IRREGULAR.put("pães", "pão");
        IRREGULAR.put("mãos", "mão");
        IRREGULAR.put("alemães", "alemão");
        IRREGULAR.put("cidadãos", "cidadão");
        IRREGULAR.put("homens", "homem");
        IRREGULAR.put("mulheres", "mulher");
        IRREGULAR.put("status", "status");
        IRREGULAR.put("males", "mal");

        // Mais específicas → mais genéricas
        addSingular("(japon|escoc|ingl|dinamarqu|fregu|portugu)eses$", "$1ês");
        addSingular("ões$", "ão");
        addSingular("ãos$", "ão");
        addSingular("ães$", "ão");
        addSingular("oes$", "ao");
        addSingular("ais$", "al");
        addSingular("éis$", "el");
        addSingular("óis$", "ol");
        addSingular("uis$", "ul");
        addSingular("([rz])es$", "$1");
        addSingular("ns$", "m");
        addSingular("ases$", "ás");
        addSingular("is$", "il");
        addSingular("([^ê])s$", "$1");
    }

    private static void addSingular(String regex, String replacement) {
        SINGULAR_RULES.add(new Rule(
                Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
                replacement
        ));
        // Observe: não invertimos aqui — a inversão é no loop de aplicação.
    }

    public static String singularize(String word) {
        if (word == null || word.isBlank()) return word;

        String lower = word.toLowerCase(Locale.ROOT);

        if (UNCOUNTABLE.contains(lower)) return word;

        for (Map.Entry<String, String> e : IRREGULAR.entrySet()) {
            if (e.getKey().equalsIgnoreCase(word)) {
                return applySameCase(word, e.getValue());
            }
        }

        for (Rule rule : SINGULAR_RULES) {
            Matcher m = rule.pattern().matcher(word);
            if (m.find()) {
                return applySameCase(word, m.replaceAll(rule.replacement()));
            }
        }
        return word;
    }

    private static String applySameCase(String original, String result) {
        if (original.equals(original.toUpperCase(Locale.ROOT))) {
            return result.toUpperCase(Locale.ROOT);
        }
        if (!original.isEmpty() && Character.isUpperCase(original.charAt(0))) {
            return Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }
        return result;
    }

    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    // Teste rápido
    public static void main(String[] args) {
        System.out.println(singularize("países"));   // país
        System.out.println(singularize("mulheres")); // mulher
        System.out.println(singularize("corações")); // coração
        System.out.println(singularize("lotações")); // lotação
        System.out.println(singularize("status"));   // status
        System.out.println(singularize("HOMENS"));   // HOMEM
        System.out.println(singularize("LOTAÇÕES")); //LOTAÇÃO
        System.out.println(singularize("PAÍSES")); //PAÍS
        System.out.println(singularize("animais")); //animal
        System.out.println(singularize("ANIMAIS")); //ANIMAL
    }
}
