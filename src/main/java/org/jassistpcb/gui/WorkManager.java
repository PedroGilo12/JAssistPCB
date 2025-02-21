package org.jassistpcb.gui;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class WorkManager {
    private Map<Object, Field> camposMonitorados = new HashMap<>();
    private Runnable listener;
    private boolean Change  = false;

    public boolean isChange() {
        return Change;
    }

    public void setChange(boolean change) {
        Change = change;
    }

    private Runnable defaultListener() {
        return () -> {
            Change = true;
            if(!MainWindow.getInstance().getTitle().endsWith("*")) {
                MainWindow.getInstance().setTitle(MainWindow.getInstance().getTitle() + "*");
            }
        };
    }

    public WorkManager() {
        this.listener = this.defaultListener();
    }

    public void addMonitoring(Object objeto, String nomeCampo) throws NoSuchFieldException {
        Field campo = objeto.getClass().getDeclaredField(nomeCampo);
        campo.setAccessible(true);
        camposMonitorados.put(objeto, campo);
        monitoring(objeto, campo);
    }

    private void monitoring(Object objeto, Field campo) {
        new Thread(() -> {
            try {
                Object valorAtual = campo.get(objeto);
                while (true) {
                    Object novoValor = campo.get(objeto);
                    if(novoValor != null) {
                        if (!novoValor.equals(valorAtual)) {
                            valorAtual = novoValor;
                            listener.run();
                        }
                        Thread.sleep(100);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
