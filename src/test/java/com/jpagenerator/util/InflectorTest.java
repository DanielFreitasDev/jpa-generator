package com.jpagenerator.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Testes da classe Inflector")
public class InflectorTest {

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'ões'")
    void testSingularizeOes() {
        assertEquals("lotação", Inflector.singularize("lotações"));
        assertEquals("situação", Inflector.singularize("situações"));
        assertEquals("função", Inflector.singularize("funções"));
        assertEquals("operação", Inflector.singularize("operações"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'oes' (sem acento)")
    void testSingularizeOesWithoutAccent() {
        assertEquals("lotacao", Inflector.singularize("lotacoes"));
        assertEquals("situacao", Inflector.singularize("situacoes"));
        assertEquals("funcao", Inflector.singularize("funcoes"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'ãos'")
    void testSingularizeAos() {
        assertEquals("irmão", Inflector.singularize("irmãos"));
        assertEquals("mão", Inflector.singularize("mãos"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'ães'")
    void testSingularizeAes() {
        assertEquals("alemão", Inflector.singularize("alemães"));
        assertEquals("cidadão", Inflector.singularize("cidadãos"));
        assertEquals("cão", Inflector.singularize("cães"));
        assertEquals("pão", Inflector.singularize("pães"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'ais'")
    void testSingularizeAis() {
        assertEquals("animal", Inflector.singularize("animais"));
        assertEquals("natural", Inflector.singularize("naturais"));
        assertEquals("comercial", Inflector.singularize("comerciais"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'éis'")
    void testSingularizeEis() {
        assertEquals("papel", Inflector.singularize("papéis"));
        assertEquals("hotel", Inflector.singularize("hotéis"));
        assertEquals("fiel", Inflector.singularize("fiéis"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'óis'")
    void testSingularizeOis() {
        assertEquals("espanhol", Inflector.singularize("espanhóis"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'uis'")
    void testSingularizeUis() {
        assertEquals("azul", Inflector.singularize("azuis"));
        assertEquals("paul", Inflector.singularize("pauis"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'is'")
    void testSingularizeIs() {
        assertEquals("funil", Inflector.singularize("funis"));
        assertEquals("barril", Inflector.singularize("barris"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'ns'")
    void testSingularizeNs() {
        assertEquals("homem", Inflector.singularize("homens"));
        assertEquals("item", Inflector.singularize("itens"));
        assertEquals("nuvem", Inflector.singularize("nuvens"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'res' e 'zes'")
    void testSingularizeResZes() {
        assertEquals("mulher", Inflector.singularize("mulheres"));
        assertEquals("luz", Inflector.singularize("luzes"));
        assertEquals("rapaz", Inflector.singularize("rapazes"));
    }

    @Test
    @DisplayName("Deve singularizar palavras terminadas em 'ases'")
    void testSingularizeAses() {
        assertEquals("gás", Inflector.singularize("gases"));
    }

    @Test
    @DisplayName("Deve singularizar nacionalidades")
    void testSingularizeNationalities() {
        assertEquals("japonês", Inflector.singularize("japoneses"));
        assertEquals("inglês", Inflector.singularize("ingleses"));
        assertEquals("português", Inflector.singularize("portugueses"));
        assertEquals("dinamarquês", Inflector.singularize("dinamarqueses"));
    }

    @Test
    @DisplayName("Deve singularizar palavras regulares terminadas em 's'")
    void testSingularizeRegularS() {
        assertEquals("carro", Inflector.singularize("carros"));
        assertEquals("casa", Inflector.singularize("casas"));
        assertEquals("livro", Inflector.singularize("livros"));
        assertEquals("mesa", Inflector.singularize("mesas"));
    }

    @Test
    @DisplayName("Deve tratar palavras irregulares")
    void testIrregularWords() {
        assertEquals("país", Inflector.singularize("países"));
        assertEquals("mal", Inflector.singularize("males"));
    }

    @Test
    @DisplayName("Deve preservar palavras incontáveis")
    void testUncountableWords() {
        assertEquals("tórax", Inflector.singularize("tórax"));
        assertEquals("tênis", Inflector.singularize("tênis"));
        assertEquals("ônibus", Inflector.singularize("ônibus"));
        assertEquals("lápis", Inflector.singularize("lápis"));
        assertEquals("fênix", Inflector.singularize("fênix"));
        assertEquals("óculos", Inflector.singularize("óculos"));
        assertEquals("vírus", Inflector.singularize("vírus"));
        assertEquals("status", Inflector.singularize("status"));
        assertEquals("atlas", Inflector.singularize("atlas"));
    }

    @Test
    @DisplayName("Deve tratar entradas null e vazias")
    void testNullAndEmptyInputs() {
        assertNull(Inflector.singularize(null));
        assertEquals("", Inflector.singularize(""));
        assertEquals("   ", Inflector.singularize("   "));
    }

    @Test
    @DisplayName("Deve preservar palavras que já estão no singular")
    void testAlreadySingularWords() {
        assertEquals("casa", Inflector.singularize("casa"));
        assertEquals("computador", Inflector.singularize("computador"));
        assertEquals("água", Inflector.singularize("água"));
    }

    @Test
    @DisplayName("Deve funcionar com case insensitive")
    void testCaseInsensitive() {
        assertEquals("LOTAÇÃO", Inflector.singularize("LOTAÇÕES"));
        assertEquals("Casa", Inflector.singularize("Casas"));
        assertEquals("ANIMAL", Inflector.singularize("ANIMAIS"));
    }

    // Testes para o metodo toPascalCase

    @Test
    @DisplayName("Deve converter snake_case para PascalCase")
    void testToPascalCase() {
        assertEquals("UserAccount", Inflector.toPascalCase("user_account"));
        assertEquals("ProductCategory", Inflector.toPascalCase("product_category"));
        assertEquals("OrderItem", Inflector.toPascalCase("order_item"));
    }

    @Test
    @DisplayName("Deve tratar strings com uma única palavra")
    void testToPascalCaseSingleWord() {
        assertEquals("User", Inflector.toPascalCase("user"));
        assertEquals("Product", Inflector.toPascalCase("product"));
        assertEquals("A", Inflector.toPascalCase("a"));
    }

    @Test
    @DisplayName("Deve tratar múltiplos underscores")
    void testToPascalCaseMultipleUnderscores() {
        assertEquals("UserAccountDetails", Inflector.toPascalCase("user_account_details"));
        assertEquals("ProductCategoryType", Inflector.toPascalCase("product_category_type"));
    }

    @Test
    @DisplayName("Deve tratar underscores consecutivos")
    void testToPascalCaseConsecutiveUnderscores() {
        assertEquals("UserAccount", Inflector.toPascalCase("user__account"));
        assertEquals("ProductCategory", Inflector.toPascalCase("product___category"));
    }

    @Test
    @DisplayName("Deve tratar entradas null e vazias no toPascalCase")
    void testToPascalCaseNullAndEmpty() {
        assertNull(Inflector.toPascalCase(null));
        assertEquals("", Inflector.toPascalCase(""));
    }

    @Test
    @DisplayName("Deve tratar strings já em PascalCase")
    void testToPascalCaseAlreadyPascal() {
        assertEquals("UserAccount", Inflector.toPascalCase("user_account"));
        assertEquals("Product", Inflector.toPascalCase("product"));
        assertEquals("TipoDado", Inflector.toPascalCase("tipo_dado"));
    }

    @Test
    @DisplayName("Deve tratar strings com números")
    void testToPascalCaseWithNumbers() {
        assertEquals("User2Account", Inflector.toPascalCase("user2_account"));
        assertEquals("Product123Category", Inflector.toPascalCase("product123_category"));
    }

    @Test
    @DisplayName("Deve tratar acentos corretamente")
    void testSingularizeWithAccents() {
        assertEquals("coração", Inflector.singularize("corações"));
        assertEquals("paixão", Inflector.singularize("paixões"));
        assertEquals("ação", Inflector.singularize("ações"));
    }

    @Test
    @DisplayName("Teste de integração - casos complexos")
    void testComplexCases() {
        // Casos que podem ter múltiplas regras aplicáveis
        assertEquals("informação", Inflector.singularize("informações"));
        assertEquals("documentação", Inflector.singularize("documentações"));
        assertEquals("configuração", Inflector.singularize("configurações"));

        // Verificar se não aplica regras desnecessárias
        assertEquals("análise", Inflector.singularize("análises"));
        assertEquals("crise", Inflector.singularize("crises"));
    }
}