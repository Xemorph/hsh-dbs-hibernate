package org.nystroem.dbs.hibernate.frontend.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class JRadioButtonPanel extends JPanel {

    private JRadioButton rdBtnChoice;
    private JTextField txtInput;

    public JRadioButtonPanel(String name, DocumentFilter filter) {
        this.rdBtnChoice = new JRadioButton((name.isEmpty() ? "Placeholder" : name));
        this.txtInput = new JTextField();
        // Set filter if provided
        if (filter != null) {
            PlainDocument doc = (PlainDocument) this.txtInput.getDocument();
            doc.setDocumentFilter(filter);
        }
        // Styling
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(this.rdBtnChoice);
        this.add(this.txtInput);

        // Set RadioButton event
        this.rdBtnChoice.addItemListener(new ItemListener() {

            @Override public void itemStateChanged(ItemEvent e) {
                handleEvent(e);
            }

        });
    }

    private void handleEvent(ItemEvent e) {
        int state = e.getStateChange();
        if (state == ItemEvent.SELECTED)
            return;
        if (state == ItemEvent.DESELECTED)
            this.txtInput.setText("");
    }

    public JRadioButton getRadioButton() {
        return this.rdBtnChoice;
    }

    public String getInputValue() {
        return this.txtInput.getText().isEmpty() ? null : this.txtInput.getText();
    }

    public void setInputValue(int value) {
        this.txtInput.setText(String.valueOf(value));
    }

}