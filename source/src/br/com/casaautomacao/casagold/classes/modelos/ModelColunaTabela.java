/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para Colunas de Tabelas
 *
 * @author Matheus Felipe Amelco <producao5@casaautomacao.com.br>
 * @date 01/02/2017 - 13:58:54
 */
public class ModelColunaTabela {

    final private String id;
    final private String nome;
    final private int tamanho;
    final private int escala;
    final private String tipo;
    final private boolean primaryKey;
    final private boolean foreignKey;
    final private boolean notNull;
    final private boolean unique;
    final private boolean computedBy;
    final private String tabela;
    final private String constraintNamePrimaryKey;
    final private String constraintNameForeignKey;
    final private String referNameTableForeignKey;
    final private String referNameColumnForeignKey;
    final private String constraintNameUnique;

    /**
     * Model para instância de Coluna de uma tabela da base de dados.
     *
     * @param id String - ID da coluna.
     * @param nome String - Nome da coluna.
     * @param tamanho int - Tamanho da coluna.
     * @param escala int - Escala da coluna.
     * @param tipo String - Tipo da coluna.
     * @param isPrimaryKey boolean - Se a coluna é uma PK.
     * @param isForeignKey boolean - Se a coluna é uma FK.
     * @param isNotNull boolean - Se a coluna é NotNull.
     * @param isUnique boolean - Se a coluna é Unique.
     * @param tabela String - Tabela onde está a coluna.
     * @param constraintNamePrimaryKey String - Nome da constraint da PK.
     * @param constraintNameForeignKey String - Nome da constraint da FK.
     * @param referNameTableForeignKey String - Nome da tabela referenciada pela
     * FK.
     * @param referNameColumnForeignKey String - Nome da coluna referenciada
     * pela FK.
     * @param constraintNameUnique String - Nome da constraint da Unique.
     */
    public ModelColunaTabela(String id, String nome, int tamanho, int escala, String tipo,
            boolean isPrimaryKey, boolean isForeignKey, boolean isNotNull, boolean isUnique,
            String tabela, String constraintNamePrimaryKey, String constraintNameForeignKey,
            String referNameTableForeignKey, String referNameColumnForeignKey, String constraintNameUnique) {
        this(id, nome, tamanho, escala, tipo, isPrimaryKey, isForeignKey, isNotNull, isUnique, tabela, constraintNamePrimaryKey, constraintNameForeignKey, referNameTableForeignKey, referNameColumnForeignKey, constraintNameUnique, false);
    }

    /**
     * Model para instância de Coluna de uma tabela da base de dados.
     *
     * @param id String - ID da coluna.
     * @param nome String - Nome da coluna.
     * @param tamanho int - Tamanho da coluna.
     * @param escala int - Escala da coluna.
     * @param tipo String - Tipo da coluna.
     * @param isPrimaryKey boolean - Se a coluna é uma PK.
     * @param isForeignKey boolean - Se a coluna é uma FK.
     * @param isNotNull boolean - Se a coluna é NotNull.
     * @param isUnique boolean - Se a coluna é Unique.
     * @param tabela String - Tabela onde está a coluna.
     * @param constraintNamePrimaryKey String - Nome da constraint da PK.
     * @param constraintNameForeignKey String - Nome da constraint da FK.
     * @param referNameTableForeignKey String - Nome da tabela referenciada pela
     * FK.
     * @param referNameColumnForeignKey String - Nome da coluna referenciada
     * pela FK.
     * @param constraintNameUnique String - Nome da constraint da Unique.
     * @param computedBy boolean - Se usa o recurso computedBy
     */
    public ModelColunaTabela(String id, String nome, int tamanho, int escala, String tipo,
            boolean isPrimaryKey, boolean isForeignKey, boolean isNotNull, boolean isUnique,
            String tabela, String constraintNamePrimaryKey, String constraintNameForeignKey,
            String referNameTableForeignKey, String referNameColumnForeignKey, String constraintNameUnique, boolean computedBy) {

        this.id = id;
        this.nome = nome;
        this.tamanho = tamanho;
        this.escala = escala;
        this.tipo = tipo;
        this.primaryKey = isPrimaryKey;
        this.foreignKey = isForeignKey;
        this.notNull = isNotNull;
        this.unique = isUnique;
        this.tabela = tabela;
        this.constraintNamePrimaryKey = constraintNamePrimaryKey;
        this.constraintNameForeignKey = constraintNameForeignKey;
        this.referNameTableForeignKey = referNameTableForeignKey;
        this.referNameColumnForeignKey = referNameColumnForeignKey;
        this.constraintNameUnique = constraintNameUnique;
        this.computedBy = computedBy;
    }

    /**
     * Retorna o ID da coluna.
     *
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna o nome da coluna.
     *
     * @return String
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o tamanho da coluna.
     *
     * @return int
     */
    public int getTamanho() {
        return tamanho;
    }

    /**
     * Retorna a escala da coluna.
     *
     * @return int
     */
    public int getEscala() {
        return escala;
    }

    /**
     * Retorna o tipo da coluna.
     *
     * @return String
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Retorna se é uma coluna PrimaryKey.
     *
     * @return boolean
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /**
     * Retorna se é uma coluna ForeignKey.
     *
     * @return boolean
     */
    public boolean isForeignKey() {
        return foreignKey;
    }

    /**
     * Retorna se é uma coluna NotNull.
     *
     * @return boolean
     */
    public boolean isNotNull() {
        return notNull;
    }

    /**
     * Retorna se é uma coluna Unique.
     *
     * @return boolean
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Retorna a tabela da coluna.
     *
     * @return String
     */
    public String getTabela() {
        return tabela;
    }

    /**
     * Retorna o nome da constraint da PK.
     *
     * @return String
     */
    public String getConstraintNamePrimaryKey() {
        return constraintNamePrimaryKey;
    }

    /**
     * Retorna o nome da constraint da FK.
     *
     * @return String
     */
    public String getConstraintNameForeignKey() {
        return constraintNameForeignKey;
    }

    /**
     * Retorna o nome da tabela referenciada pela FK.
     *
     * @return String
     */
    public String getReferNameTableForeignKey() {
        return referNameTableForeignKey;
    }

    /**
     * Retorna o nome da coluna referenciada pela FK.
     *
     * @return String
     */
    public String getReferNameColumnForeignKey() {
        return referNameColumnForeignKey;
    }

    /**
     * Retorna o nome da constraint da coluna Unique.
     *
     * @return String
     */
    public String getConstraintNameUnique() {
        return constraintNameUnique;
    }

    /**
     * Retorna se a coluna usa o recurso computedBy
     *
     * @return
     */
    public boolean isComputedBy() {
        return computedBy;
    }

}
