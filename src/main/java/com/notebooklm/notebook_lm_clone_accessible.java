// notebook_lm_clone_accessible.java
// Core UI class with accessibility, tab focus, and hotkeys

package com.notebooklm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class NotebookLMClone extends JFrame {
    private JButton selectFolderButton;
    private JTextField queryField;
    private JButton sendButton;
    private JList<String> fileList;
    private JTextArea chatArea;

    public NotebookLMClone() {
        super("NotebookLM Clone - Accessible");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectFolderButton = new JButton("Select Folder");
        selectFolderButton.setMnemonic(KeyEvent.VK_P); // Alt+P
        selectFolderButton.getAccessibleContext().setAccessibleName("Select Project Folder");
        topPanel.add(selectFolderButton);

        JLabel queryLabel = new JLabel("Query:");
        queryField = new JTextField(40);
        queryField.getAccessibleContext().setAccessibleName("Query input field");
        queryLabel.setLabelFor(queryField);
        topPanel.add(queryLabel);
        topPanel.add(queryField);

        sendButton = new JButton("Send");
        sendButton.setMnemonic(KeyEvent.VK_S); // Alt+S
        sendButton.getAccessibleContext().setAccessibleDescription("Send the query");
        topPanel.add(sendButton);

        add(topPanel, BorderLayout.NORTH);

        fileList = new JList<>(new DefaultListModel<>());
        fileList.getAccessibleContext().setAccessibleName("Project file list");
        JScrollPane fileScroll = new JScrollPane(fileList);
        fileScroll.setPreferredSize(new Dimension(200, 0));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.getAccessibleContext().setAccessibleDescription("Chat history area");
        JScrollPane chatScroll = new JScrollPane(chatArea);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileScroll, chatScroll);
        add(splitPane, BorderLayout.CENTER);

        setFocusTraversalPolicy(new CustomFocusTraversalPolicy(List.of(
            selectFolderButton, queryField, sendButton, fileList
        )));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NotebookLMClone::new);
    }

    static class CustomFocusTraversalPolicy extends FocusTraversalPolicy {
        private final List<Component> order;

        public CustomFocusTraversalPolicy(List<Component> order) {
            this.order = order;
        }

        public Component getComponentAfter(Container a, Component c) {
            int i = (order.indexOf(c) + 1) % order.size();
            return order.get(i);
        }

        public Component getComponentBefore(Container a, Component c) {
            int i = order.indexOf(c) - 1;
            if (i < 0) i = order.size() - 1;
            return order.get(i);
        }

        public Component getFirstComponent(Container a) { return order.get(0); }
        public Component getLastComponent(Container a) { return order.get(order.size() - 1); }
        public Component getDefaultComponent(Container a) { return order.get(0); }
    }
}