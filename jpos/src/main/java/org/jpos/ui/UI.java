/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.ui;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jpos.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Alejandro Revilla
 *
 * <p>jPOS UI main class</p>
 *
 * @see UIFactory
 *
 * See src/examples/ui/* for usage details
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class UI implements UIFactory, UIObjectFactory {
    JFrame mainFrame;
    Map registrar, mapping;
    Element config;
    UIObjectFactory objFactory;

    Log log;
    boolean destroyed = false;
    static final ResourceBundle classMapping;

    static {
        classMapping = ResourceBundle.getBundle(UI.class.getName());
    }
    /**
     * Create a new UI object
     */
    public UI () {
        super ();
        registrar = new HashMap ();
        mapping = new HashMap ();
        setObjectFactory (this);
    }
    /**
     * Creates a new UI object
     * @param config configuration element
     */
    public UI (Element config) {
        this ();
        setConfig(config);
    }
    /**
     * Assigns an object factory use to create new object instances.
     * If no object factory is asigned, UI uses the default classloader
     *
     * @param objFactory reference to an Object Factory
     */
    public void setObjectFactory (UIObjectFactory objFactory) {
        this.objFactory = objFactory;
    }
    /**
     * @param config the Configuration element
     */
    public void setConfig (Element config) {
        this.config = config;
    }
    /**
     * @param log an optional Log instance
     * @see org.jpos.util.Log
     */
    public void setLog (Log log) {
        this.log = log;
    }
    public Log getLog () {
        return log;
    }
    /**
     * UI uses a map to hold references to its components
     * ("id" attribute)
     *
     * @return UI component registrar
     */
    public Map getRegistrar () {
        return registrar;
    }
    /**
     * @param id Component id ("id" configuration attribute)
     * @return the Object or null
     */
    public Object get (String id) {
        return registrar.get (id);
    }
   /**
    * UI is itself a UIFactory. 
    * This strategy is used to recursively instantiate components
    * inside a container
    * 
    * @param ui reference to this UI instance
    * @param e free form configuration Element
    * @return JComponent
    */
    public JComponent create (UI ui, Element e) {
        return create(e);
    }
    /**
     * UIObjectFactory implementation.
     * uses default classloader
     * @param clazz the Clazzzz
     * @return the Object
     * @throws Exception if unable to instantiate
     * @see #setLog
     */
    public Object newInstance (String clazz) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader ();
        Class type = cl.loadClass (clazz);
        return type.newInstance();
    }
    /**
     * configure this UI object
     */
    public void configure () throws JDOMException {
        configure (config);
    } 
    /**
     * reconfigure can be used in order to re-configure components
     * inside a container (i.e. changing a panel in response to
     * an event).
     * @see org.jpos.ui.action.Redirect
     *
     * @param elementName the element name used as new configuration
     * @param panelName panel ID (see "id" attribute)
     */
    public void reconfigure (String elementName, String panelName) {
        Container c = 
            panelName == null ? mainFrame.getContentPane() : (JComponent) get (panelName);
        if (c != null) {
            c.removeAll ();
            c.add (
                createComponent (config.getChild (elementName))
            );
            if (c instanceof JComponent) {
                c.revalidate();
            }
            c.repaint ();
        }
    }
    /**
     * dispose this UI object
     */
    public void dispose () {
     /* This is the right code for the dispose, but it freezes in
        JVM running under WinXP (in linux went fine.. I didn't 
        test it under other OS's)
        (last version tested: JRE 1.5.0-beta2)
  
        if (mainFrame != null) {
            // dumpComponent (mainFrame);
            mainFrame.dispose ();
     */
        destroyed = true;

        Iterator it = Arrays.asList(Frame.getFrames()).iterator();

        while (it.hasNext()) {
            JFrame jf = (JFrame) it.next();
            removeComponent(jf);
        }
    }
    /**
     * @return true if this UI object has been disposed and is no longer valid
     */
    public boolean isDestroyed () {
        return destroyed;
    }

    protected void configure (Element ui) throws JDOMException {
        setLookAndFeel (ui);
        createMappings (ui);
        createObjects (ui, "object");
        createObjects (ui, "action");
        if (!"ui".equals (ui.getName())) {
            ui = ui.getChild ("ui");
        }
        if (ui != null) {
            JFrame frame = initFrame (ui);
            Element mb = ui.getChild ("menubar");
            if (mb != null) 
                frame.setJMenuBar (buildMenuBar (mb));

            frame.setContentPane (
                createComponent (ui.getChild ("components"))
            );
            if ("true".equals (ui.getAttributeValue ("full-screen"))) {
                GraphicsDevice device = GraphicsEnvironment
                                            .getLocalGraphicsEnvironment()
                                            .getDefaultScreenDevice();
                frame.setUndecorated (
                    "true".equals (ui.getAttributeValue ("undecorated"))
                );
                device.setFullScreenWindow(frame);
            } else {
                frame.show ();
            }
        }
    }

    private void removeComponent (Component c) {
        if (c instanceof Container) {
            Container cont = (Container) c;
            Component[] cc = cont.getComponents();

            for (Component aCc : cc) {
                removeComponent(aCc);
            }
            cont.removeAll();
        }
    }

    // ##DEBUG##
    private void dumpComponent (Component c) {
        System.out.println (c.getClass().getName() + ":" + c.getBounds().getSize().toString());
        if (c instanceof Container) {
            Component[] cc = ((Container) c).getComponents();
            for (Component aCc : cc) {
                dumpComponent(aCc);
            }
        }
    }

    private JFrame initFrame (Element ui) {
        Element caption = ui.getChild ("caption");
        mainFrame = caption == null ?  
            new JFrame () :
            new JFrame (caption.getText());

        JOptionPane.setRootFrame (mainFrame);

        mainFrame.getContentPane().setLayout(new BorderLayout());

        String close = ui.getAttributeValue ("close");

        if ("false".equals (close))
            mainFrame.setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        else if ("exit".equals (close))
            mainFrame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(getDimension (ui, screenSize));
        locateOnScreen(mainFrame);
        return mainFrame;
    }

    private void locateOnScreen(Frame frame) {
        Dimension paneSize   = frame.getSize();
        Dimension screenSize = frame.getToolkit().getScreenSize();
        frame.setLocation(
                (screenSize.width - paneSize.width) / 2,
                (screenSize.height - paneSize.height) / 2);
    }
    private JMenuBar buildMenuBar (Element ui) {
        JMenuBar mb = new JMenuBar ();
        Iterator iter = ui.getChildren("menu").iterator();
        while (iter.hasNext()) 
            mb.add (menu ((Element) iter.next()));

        return mb;
    }
    private JMenu menu (Element m) {
        JMenu menu = new JMenu (m.getAttributeValue ("id"));
        setItemAttributes (menu, m);
        Iterator iter = m.getChildren ().iterator();
        while (iter.hasNext()) 
            addMenuItem(menu, (Element) iter.next());
        return menu;
    }
    private void addMenuItem (JMenu menu, Element m) {
        String tag = m.getName ();

        if ("menuitem".equals (tag)) {
            JMenuItem item = new JMenuItem (m.getAttributeValue ("id"));
            setItemAttributes (item, m);
            menu.add (item);
        } else if ("menuseparator".equals (tag)) {
            menu.addSeparator ();
        } else if ("button-group".equals (tag)) {
            addButtonGroup (menu, m);
        } else if ("check-box".equals (tag)) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem (
                m.getAttributeValue ("id")
            );
            setItemAttributes (item, m);
            item.setState (
                "true".equals (m.getAttributeValue ("state"))
            );
            menu.add (item);
        } else if ("menu".equals (tag)) {
            menu.add (menu (m));
        }
    }
    private void addButtonGroup (JMenu menu, Element m) {
        ButtonGroup group = new ButtonGroup();
        Iterator iter = m.getChildren ("radio-button").iterator();
        while (iter.hasNext()) {
            addRadioButton (menu, group, (Element) iter.next());
        }
    }
    private void addRadioButton (JMenu menu, ButtonGroup group, Element m) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem
            (m.getAttributeValue ("id"));
        setItemAttributes (item, m);
        item.setSelected (
            "true".equals (m.getAttributeValue ("selected"))
        );
        group.add (item);
        menu.add (item);
    }
    private Dimension getDimension (Element e, Dimension def) {
        String w = e.getAttributeValue ("width");
        String h = e.getAttributeValue ("height");

        return new Dimension (
           w != null ? Integer.parseInt (w) : def.width,
           h != null ? Integer.parseInt (h) : def.height
        );
    }
    private void setItemAttributes (AbstractButton b, Element e) 
    {
        String s = e.getAttributeValue ("accesskey");
        if (s != null && s.length() == 1)
            b.setMnemonic (s.charAt(0));

        String icon = e.getAttributeValue ("icon");
        if (icon != null) {
            try {
                b.setIcon (new ImageIcon (new URL (icon)));
            } catch (MalformedURLException ex) {
                ex.printStackTrace ();
            }
        }
        b.setActionCommand (e.getAttributeValue ("command"));
        String actionId = e.getAttributeValue ("action");
        if (actionId != null) {
            b.addActionListener ((ActionListener) get (actionId));
        }
    }
    protected void setLookAndFeel (Element ui) {
        String laf = ui.getAttributeValue ("look-and-feel");
        if (laf != null) {
            try {
                UIManager.setLookAndFeel (laf);
            } catch (Exception e) {
                warn (e);
            }
        }
    }
    private JComponent createComponent (Element e) {
        if (e == null)
            return new JPanel ();

        JComponent component;
        UIFactory factory = null;
        String clazz = e.getAttributeValue ("class");
        if (clazz == null) 
            clazz = (String) mapping.get (e.getName());
        if (clazz == null) {
            try {
                clazz = classMapping.getString (e.getName());
            } catch (MissingResourceException ignored) {
                // OK to happen on components handled by this factory
            }
        }
        try {
            if (clazz == null) 
                factory = this;
            else 
                factory = (UIFactory) objFactory.newInstance (clazz.trim());

            component = factory.create (this, e);
            setSize (component, e);
            if (component instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) component;
                b.setActionCommand (e.getAttributeValue ("command"));
                String actionId = e.getAttributeValue ("action");
                if (actionId != null) {
                    b.addActionListener ((ActionListener) get (actionId));
                }
            }
            put (component, e);

            Element script = e.getChild ("script");
            if (script != null) 
                component = doScript (component, script);

            if ("true".equals (e.getAttributeValue ("scrollable")))
                component = new JScrollPane (component);
        } catch (Exception ex) {
            warn ("Error instantiating class " + clazz);
            warn (ex);
            component = new JLabel ("Error instantiating class " + clazz);
        }
        return component;
    }
    protected JComponent doScript (JComponent component, Element e) {
        return component;
    }
    private void setSize (JComponent c, Element e) {
        String w = e.getAttributeValue ("width");
        String h = e.getAttributeValue ("height");
        Dimension d = c.getPreferredSize ();
        double dw = d.getWidth ();
        double dh = d.getHeight ();
        if (w != null) 
            dw = Double.parseDouble (w);
        if (h != null) 
            dh = Double.parseDouble (h);
        if (w != null || h != null) {
            d.setSize (dw, dh);
            c.setPreferredSize (d);
        }
    }
    public JComponent create (Element e) {
        JComponent component = null;

        Iterator iter = e.getChildren().iterator();
        for (int i=0; iter.hasNext(); i++) {
            JComponent c = createComponent((Element) iter.next ());
            if (i == 0)
                component = c;
            else if (i == 1) {
                JPanel p = new JPanel ();
                p.add (component);
                p.add (c);
                component = p;
                put (component, e);
            } else {
                component.add (c);
            }
        }
        return component;
    }
    public JFrame getMainFrame() {
        return mainFrame;
    }
    
    private void createObjects (Element e, String name) {
        Iterator iter = e.getChildren (name).iterator ();
        while (iter.hasNext()) {
            try {
                Element ee = (Element) iter.next ();
                String clazz = ee.getAttributeValue ("class");
                Object obj = objFactory.newInstance (clazz.trim());
                if (obj instanceof UIAware) {
                    ((UIAware) obj).setUI (this, ee);
                }
                put (obj, ee);
            } catch (Exception ex) {
                warn (ex);
            }
        }
    }
    private void createMappings (Element e) {
        Iterator iter = e.getChildren ("mapping").iterator ();
        while (iter.hasNext()) {
            try {
                Element ee = (Element) iter.next ();
                String name  = ee.getAttributeValue ("name");
                String clazz = ee.getAttributeValue("factory");
                mapping.put(name, clazz);
            } catch (Exception ex) {
                warn (ex);
            }
        }
    }
    protected void warn (Object obj) {
        if (log != null)
            log.warn (obj);
    }
    protected void warn (Object obj, Exception ex) {
        if (log != null)
            log.warn (obj, ex);
    }

    private void put (Object obj, Element e) {
        String id = e.getAttributeValue ("id");
        if (id != null) {
            registrar.put (id, obj);
        }
    }
}

