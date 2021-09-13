package jp.ngt.rtm.gui;

import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.modelpack.state.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DataMapEditor extends JFrame {
    private static final int DATA_COL = 2;

    private final GuiSelectModel parentGui;
    private final Map<String, IDataFilter> filterMap = new HashMap<>();
    private final DataFormatter formatter = new DataFormatter();

    public DataMapEditor(GuiSelectModel gui) {
        this.parentGui = gui;
        this.init();
        this.setVisible(true);
    }

    private void init() {
        int scale = NGTUtilClient.getMinecraft().displayHeight / 720;
        scale = (scale <= 0) ? 1 : scale;//マルチで負値になるので
        int fontSize = 16 * scale;
        Font font = new Font("Arial", Font.PLAIN, fontSize);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setBounds(32, 32, 480 * scale, 640 * scale);
        this.setTitle("DataMap Editor");

        ResourceState state = this.parentGui.selector.getResourceState();
        //ソート用
        Map<String, DataEntry> map = new TreeMap<>(state.dataMap.getEntries());
        String[][] rowData = new String[map.size()][3];
        int i = 0;
        for (Entry<String, DataEntry> entry : map.entrySet()) {
            rowData[i++] = new String[]{entry.getKey(), entry.getValue().getType().key, entry.getValue().toString()};
            IDataFilter filter = formatter.getFilter(entry.getKey());
            if (filter != null) {
                this.filterMap.put(entry.getKey(), filter);
            }
        }

        String[] columnNames = {"Key", "Type", "Data"};
        JTable table = new DMEditorTable(font, fontSize, rowData, columnNames);

        JScrollPane scrollPane = new JScrollPane(table);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton buttonOK = new JButton("OK");
        buttonOK.setFont(font);
        buttonOK.addActionListener((event) -> {
            for (int j = 0; j < rowData.length; ++j) {
                String key = (String) table.getModel().getValueAt(j, 0);
                String value = String.format("(%s)%s",
                        table.getModel().getValueAt(j, 1),
                        table.getModel().getValueAt(j, 2));
                state.dataMap.set(key, value, DataMap.SYNC_FLAG | DataMap.SAVE_FLAG);
            }
            this.parentGui.argField.setText(state.getArg());
            this.parentGui.saveData(state);
            this.dispose();
        });
        buttonPanel.add(buttonOK);

        JButton buttonCancel = new JButton("Cancel");
        buttonCancel.setFont(font);
        buttonCancel.addActionListener(event -> this.dispose());
        buttonPanel.add(buttonCancel);

        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    public class DMEditorTable extends JTable {
        public DMEditorTable(Font font, int fontSize, String[][] rowData, String[] columnNames) {
            super(new DefaultTableModel(rowData, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == DATA_COL;//Data列以外は編集不可
                }

                @Override
                public void setValueAt(Object aValue, int row, int column) {
                    if (column == DATA_COL) {
                        String key = rowData[row][0];
                        IDataFilter filter = DataMapEditor.this.filterMap.get(key);
                        if (filter != null && !filter.check(aValue)) {
                            return;
                        }
                    }
                    super.setValueAt(aValue, row, column);
                }
            });

            this.setFont(font);
            this.setRowHeight(fontSize);

            String[][] array = new String[rowData.length][];
            for (int i = 0; i < array.length; ++i) {
                String[] suggestions = formatter.getSuggestions(rowData[i][0]);
                if (suggestions == null || suggestions.length == 0) {
                    array[i] = new String[0];
                } else {
                    array[i] = suggestions;
                }
            }
            this.getColumnModel().getColumn(DataMapEditor.DATA_COL).setCellEditor(new DMECellEditor(font, array));
        }
    }

    /**
     * 入力候補が複数の場合はComboBox、そうでない場合はTextFieldを使う
     */
    public class DMECellEditor extends AbstractCellEditor implements TableCellEditor {
        /**
         * [row][num]
         */
        private final String[][] suggestions;

        private JTextField text;
        private JComboBox combo;
        private Object value;
        private Component editor;

        public DMECellEditor(Font font, String[][] sug) {
            this.suggestions = sug;

            this.text = new JTextField();
            this.text.setFont(font);
            this.text.setBorder(BorderFactory.createEmptyBorder());
            this.combo = new JComboBox();
            this.combo.setFont(font);
            this.combo.setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public Object getCellEditorValue() {
            //return this.value;
            return this.getCellValue();
        }

        @Override
        public boolean stopCellEditing() {
            if (this.editor instanceof JComboBox) {
                if (this.combo.isEditable()) {
                    this.combo.actionPerformed(new ActionEvent(this, 0, ""));
                }
            }
            this.value = this.getCellValue();
            return super.stopCellEditing();
        }

        private Object getCellValue() {
            if (this.editor instanceof JComboBox) {
                return ((JComboBox) this.editor).getSelectedItem();
            } else if (this.editor instanceof JTextField) {
                return ((JTextField) this.editor).getText();
            }
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            String[] sa = this.suggestions[row];
            if (sa.length > 0) {
                this.combo.removeAllItems();
                for (String s : sa) {
                    this.combo.addItem(s);
                }
                this.combo.setSelectedItem(value);
                this.editor = this.combo;
            } else {
                this.text.setText(value.toString());
                this.editor = text;
            }
            return this.editor;
        }
    }
}
