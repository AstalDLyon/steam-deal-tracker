package com.tracker.client.view;

import com.tracker.client.http.BackendApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class JanelaPrincipal extends JFrame {

    private javax.swing.table.TableRowSorter<DefaultTableModel> sorter;
    private final BackendApiClient apiClient;

    private JTable tabelaJogos;
    private DefaultTableModel modeloTabela;
    private JLabel labelStatus;
    private JButton btnAtualizar;

    public JanelaPrincipal(BackendApiClient apiClient) {
        this.apiClient = apiClient;
        configurarJanela();
        construirInterface();
    }

    private void configurarJanela() {
        setTitle("Steam Deal Tracker");
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));
    }

    private void construirInterface() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Cabeçalho
        JPanel painelTopo = new JPanel(new BorderLayout());
        JLabel titulo = new JLabel("Jogos Monitorados");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));

        labelStatus = new JLabel("Pronto");
        labelStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelStatus.setForeground(Color.GRAY);

        btnAtualizar = new JButton("Atualizar");
        btnAtualizar.addActionListener(e -> atualizarLista());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        painelBotoes.add(btnAtualizar);

        painelTopo.add(titulo, BorderLayout.WEST);
        painelTopo.add(labelStatus, BorderLayout.CENTER);
        painelTopo.add(painelBotoes, BorderLayout.EAST);

        // Tabela
        String[] colunas = {
                "Nome", "AppID", "Preço Atual", "Mínimo Histórico", "Última Verificação"
        };

        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabela somente leitura
            }
        };

        tabelaJogos = new JTable(modeloTabela);
        // Após: tabelaJogos = new JTable(modeloTabela);
        sorter = new javax.swing.table.TableRowSorter<>(modeloTabela);
        tabelaJogos.setRowSorter(sorter);

// Ordenação padrão: nome A-Z ao iniciar
        sorter.setSortKeys(List.of(
                new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING)
        ));

// Definir comparador numérico para colunas de preço
// (sem isso, ordena como texto: "R$ 9.99" vem depois de "R$ 99.99")
        sorter.setComparator(2, Comparator.comparing(o -> {
            String s = o.toString().replace("R$ ", "").replace(",", ".");
            try { return new java.math.BigDecimal(s); }
            catch (Exception e) { return java.math.BigDecimal.ZERO; }
        }));

        sorter.setComparator(3, Comparator.comparing(o -> {
            String s = o.toString().replace("R$ ", "").replace(",", ".");
            try { return new java.math.BigDecimal(s); }
            catch (Exception e) { return java.math.BigDecimal.ZERO; }
        }));
        tabelaJogos.setRowHeight(28);
        tabelaJogos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaJogos.getTableHeader().setReorderingAllowed(false);

        // Larguras das colunas
        tabelaJogos.getColumnModel().getColumn(0).setPreferredWidth(220); // Nome
        tabelaJogos.getColumnModel().getColumn(1).setPreferredWidth(80);  // AppID
        tabelaJogos.getColumnModel().getColumn(2).setPreferredWidth(100); // Preço Atual
        tabelaJogos.getColumnModel().getColumn(3).setPreferredWidth(120); // Mínimo Histórico
        tabelaJogos.getColumnModel().getColumn(4).setPreferredWidth(150); // Última Verificação

        JScrollPane scrollPane = new JScrollPane(tabelaJogos);

        // Rodapé
        JPanel painelRodape = new JPanel(new BorderLayout());
        JLabel labelDica = new JLabel(
                "💡 O programa monitora sua wishlist automaticamente a cada 3 horas."
        );
        labelDica.setFont(new Font("SansSerif", Font.ITALIC, 11));
        labelDica.setForeground(Color.GRAY);
        painelRodape.add(labelDica, BorderLayout.WEST);

        painelPrincipal.add(painelTopo, BorderLayout.NORTH);
        painelPrincipal.add(scrollPane, BorderLayout.CENTER);
        painelPrincipal.add(painelRodape, BorderLayout.SOUTH);

        setContentPane(painelPrincipal);
    }

    public void atualizarLista() {
        labelStatus.setText("Atualizando...");
        btnAtualizar.setEnabled(false);

        // Roda em thread separada para não travar a UI
        SwingWorker<List<Map<String, Object>>, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected List<Map<String, Object>> doInBackground() {
                        return apiClient.buscarJogosMonitorados();
                    }

                    @Override
                    protected void done() {
                        try {
                            List<Map<String, Object>> jogos = get();
                            modeloTabela.setRowCount(0); // limpa a tabela

                            for (Map<String, Object> jogo : jogos) {
                                String nome = (String) jogo.getOrDefault("nome", "—");
                                Object appId = jogo.getOrDefault("steamAppId", "—");
                                Object precoAtual = formatarPreco(jogo.get("precoAtual"));
                                Object minimoHistorico = formatarPreco(jogo.get("menorPrecoHistorico"));
                                String ultimaVerificacao = formatarData(
                                        jogo.getOrDefault("ultimaVerificacao", "—").toString()
                                );

                                modeloTabela.addRow(new Object[]{
                                        nome, appId, precoAtual, minimoHistorico, ultimaVerificacao
                                });
                            }

                            labelStatus.setText(jogos.size() + " jogo(s) monitorado(s)");
                        } catch (Exception e) {
                            labelStatus.setText("Erro ao atualizar");
                        } finally {
                            btnAtualizar.setEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private String formatarPreco(Object preco) {
        if (preco == null) return "—";
        return "R$ " + preco;
    }

    private String formatarData(String dataHora) {
        if (dataHora == null || dataHora.equals("—")) return "—";
        // Remove os nanosegundos para exibição limpa
        if (dataHora.contains("T")) {
            String[] partes = dataHora.split("T");
            String hora = partes[1].length() > 8 ? partes[1].substring(0, 8) : partes[1];
            return partes[0] + " " + hora;
        }
        return dataHora;
    }
}