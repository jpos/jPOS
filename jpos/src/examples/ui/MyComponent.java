package ui;

import java.awt.Font;
import javax.swing.*;
import org.jdom.Element;
import org.jpos.ui.UI;
import org.jpos.ui.UIFactory;

/**
 * @author Alejandro Revilla
 *
 * Demoes a user created component
 *
 * <pre>
 *  &lt;my-component"&gt;Custom Component&lt;/my-component&gt;
 * </pre>
 * @see org.jpos.ui.UIFactory
 */
public class MyComponent implements UIFactory {
    public JComponent create (UI ui, Element e) {
        JLabel label = new JLabel (e.getText());
        String font = e.getAttributeValue ("font");
        if (font != null) 
            label.setFont (Font.decode (font));
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
}

