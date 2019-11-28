package org.nystroem.dbs.hibernate.frontend.gui;

import java.util.List;

import javax.swing.JOptionPane;

import org.nystroem.dbs.hibernate.frontend.logic.PersonManager;

public class CharacterDialogCallback {

    private PersonManager pm = new PersonManager();

    public List<String> getPersonList(String text) {
        try {
            List<String> persons = pm.getPersonList(text);
            return persons;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fehler bei der Personensuche: \n" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
