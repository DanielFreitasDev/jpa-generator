# Gerador Automático de Classes JPA

Este projeto oferece uma ferramenta completa para automatizar a geração de classes JPA (Entity) a partir do esquema de banco de dados PostgreSQL, seguindo padrões consistentes e oferecendo flexibilidade na configuração.

## 🚀 Características

- **Conexão Automática**: Conecta com PostgreSQL usando configurações persistentes
- **Geração Inteligente**: Gera classes JPA seguindo as melhores práticas
- **Relacionamentos**: Suporte completo para Foreign Keys (como colunas ou relacionamentos JPA)
- **Compatibilidade**: Suporte para Java 8+ (javax) e Java 17+ (jakarta)
- **Interface Amigável**: CLI interativo e modo batch
- **Configuração Persistente**: Salva configurações para reutilização

## 📋 Pré-requisitos

- Java 17+ (recomendado) ou Java 8+
- Maven 3.6+
- PostgreSQL 10+
- Acesso ao banco de dados PostgreSQL

## 🔧 Instalação

1. Clone o repositório:
```bash
git clone https://github.com/DanielFreitasDev/jpa-generator.git
cd jpa-generator
```

2. Compile o projeto:
```bash
mvn clean package
```

3. O arquivo executável será gerado em `target/jpa-generator.jar`

## 🎯 Uso

### Modo Interativo (Recomendado)
```bash
java -jar target/jpa-generator.jar --interactive
```

### Modo Batch

**Gerar todas as tabelas de um schema:**
```bash
java -jar target/jpa-generator.jar --schema desmonte
```

**Gerar tabela específica:**
```bash
java -jar target/jpa-generator.jar --table desmonte.empresas
```

**Usar configuração específica:**
```bash
java -jar target/jpa-generator.jar --config custom-config.json --schema desmonte
```

**Especificar diretório de saída:**
```bash
java -jar target/jpa-generator.jar --schema desmonte --output src/main/java
```

## ⚙️ Configuração

Na primeira execução, a ferramenta solicitará as informações de conexão:

- **Host**: Endereço do servidor PostgreSQL (padrão: localhost)
- **Porta**: Porta do PostgreSQL (padrão: 5432)
- **Database**: Nome do banco de dados
- **Usuário**: Nome de usuário
- **Senha**: Senha do usuário
- **Versão Java**: 8 (javax) ou 17+ (jakarta)
- **Diretório de Saída**: Onde salvar as classes geradas
- **Package Base**: Package base para as classes

As configurações são salvas em `config/database.json` para reutilização.

### Exemplo de Configuração
```json
{
  "host": "localhost",
  "port": 5432,
  "database": "meu_banco",
  "username": "postgres",
  "password": "senha123",
  "javaVersion": 17,
  "outputDirectory": "src/main/java",
  "basePackage": "com.example.entity",
  "generateAuditFields": true,
  "useLombok": true
}
```

## 🏗️ Estrutura Gerada

### Exemplo de Classe Simples

**Tabela SQL:**
```sql
create table perfis (
    id         smallserial,
    descricao  varchar(100) not null,
    operador   varchar(50)  not null,
    created_at timestamp(6) not null,
    updated_at timestamp(6),
    constraint perfil_pk primary key (id)
);
```

**Classe Java Gerada:**
```java
@Getter
@Setter
@Entity
@Table(name = "perfis", schema = "desmonte")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "perfis_id_gen")
    @SequenceGenerator(name = "perfis_id_gen", sequenceName = "desmonte.perfis_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "descricao", nullable = false, length = 100)
    private String descricao;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

### Tratamento de Foreign Keys

Para cada Foreign Key detectada, você pode escolher:

1. **Coluna Simples**: Mantém como campo Long/Integer
2. **Relacionamento JPA**: Cria relacionamento @ManyToOne

**Relacionamento JPA:**
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "status_cartela_id", nullable = false)
private StatusCartela statusCartela;
```

## 🔄 Mapeamento de Tipos

| PostgreSQL | Java |
|------------|------|
| smallint, smallserial | Integer |
| integer, serial | Integer |
| bigint, bigserial | Long |
| varchar, text | String |
| boolean | Boolean |
| timestamp | Instant |
| date | LocalDate |
| time | LocalTime |
| numeric, decimal | BigDecimal |
| real | Float |
| double precision | Double |
| uuid | UUID |

## 📁 Estrutura do Projeto

```
jpa-generator/
├── config/
│   └── database.json          # Configuração persistente
├── src/
│   └── main/
│       └── java/
│           └── com/jpagenerator/
│               ├── Main.java               # Classe principal
│               ├── config/
│               │   ├── DatabaseConfig.java # Configuração
│               │   └── ConfigManager.java  # Gerenciador de config
│               ├── inspector/
│               │   └── DatabaseInspector.java # Inspetor de BD
│               ├── generator/
│               │   └── CodeGenerator.java     # Gerador de código
│               └── model/
│                   └── *.java              # Classes de modelo
├── target/
│   └── jpa-generator.jar      # Executável gerado
├── pom.xml
└── README.md
```

## 🎨 Funcionalidades Avançadas

### Valores Padrão
Automaticamente detecta e mapeia valores padrão usando `@ColumnDefault`:

```java
@Size(max = 2)
@ColumnDefault("'CE'")
@Column(name = "uf", length = 2)
private String uf;
```

### Sequences PostgreSQL
Detecta automaticamente sequences e configura adequadamente:

```java
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empresas_id_gen")
@SequenceGenerator(name = "empresas_id_gen", sequenceName = "desmonte.empresas_id_seq", allocationSize = 1)
```

### Validação Bean Validation
Adiciona automaticamente validações baseadas nas constraints do banco:

```java
@Size(max = 100)
@NotNull
@Column(name = "descricao", nullable = false, length = 100)
private String descricao;
```

## 🔧 Opções de Linha de Comando

| Opção | Descrição | Exemplo |
|-------|-----------|---------|
| `--interactive` | Executa em modo interativo | `--interactive` |
| `--schema` | Processa todas as tabelas do schema | `--schema desmonte` |
| `--table` | Processa tabela específica | `--table desmonte.empresas` |
| `--config` | Usa arquivo de configuração específico | `--config custom.json` |
| `--output` | Define diretório de saída | `--output src/main/java` |

## 🐛 Solução de Problemas

### Erro de Conexão
```
Erro: Connection refused
```
**Solução**: Verifique se o PostgreSQL está rodando e as credenciais estão corretas.

### Erro de Permissão
```
Erro: permission denied for schema
```
**Solução**: Certifique-se de que o usuário tem permissões de leitura no schema.

### Classes não compilam
```
Erro: cannot find symbol
```
**Solução**: Verifique se as dependências JPA estão no classpath do seu projeto:

```xml
<!-- Para Java 17+ -->
<dependency>
    <groupId>jakarta.persistence</groupId>
    <artifactId>jakarta.persistence-api</artifactId>
    <version>3.1.0</version>
</dependency>

<!-- Para Java 8-16 -->
<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
    <version>2.2</version>
</dependency>
```

## 🔄 Migração entre Versões Java

### De javax para jakarta (Java 17+)

O gerador detecta automaticamente a versão do Java e usa os imports corretos:

**Java 8-16:**
```java
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
```

**Java 17+:**
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
```

## 📈 Roadmap

- [ ] Suporte para relacionamentos @OneToMany
- [ ] Geração de DTOs
- [ ] Geração de Repositories
- [ ] Suporte para outros bancos (MySQL, Oracle)
- [ ] Interface gráfica (GUI)
- [ ] Plugin Maven/Gradle
- [ ] Templates customizáveis
- [ ] Auditoria automática (created_by, updated_by)

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Padrões de Código

- Use Java 17+ features quando possível
- Siga as convenções de nomenclatura Java
- Adicione testes para novas funcionalidades
- Documente métodos públicos

## 📝 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📞 Suporte

Para suporte e dúvidas:

- 🐛 **Issues**: [GitHub Issues](https://github.com/seu-usuario/jpa-generator/issues)
- 📧 **Email**: seu-email@exemplo.com
- 💬 **Discussões**: [GitHub Discussions](https://github.com/seu-usuario/jpa-generator/discussions)

## 🙏 Agradecimentos

- Spring Boot Team pela inspiração
- Hibernate Team pelas annotations JPA
- PostgreSQL Team pelo excelente banco de dados
- Lombok Project pela redução de boilerplate

---

**⚡ Dica**: Para melhor experiência, use o modo interativo na primeira execução para se familiarizar com as opções disponíveis!