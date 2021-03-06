/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.automation.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.automation.Module;
import org.osgi.framework.BundleContext;

/**
 * This is a base class that can be used by any ModuleHandlerFactory implementation
 *
 * @author Kai Kreuzer - Initial Contribution
 * @author Benedikt Niehues - change behavior for unregistering ModuleHandler
 */
@NonNullByDefault
public abstract class BaseModuleHandlerFactory implements ModuleHandlerFactory {

    private final Map<String, ModuleHandler> handlers = new HashMap<String, ModuleHandler>();

    @NonNullByDefault({})
    protected BundleContext bundleContext;

    public void activate(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new IllegalArgumentException("BundleContext must not be null.");
        }
        this.bundleContext = bundleContext;
    }

    public void deactivate() {
        dispose();
    }

    protected Map<String, ModuleHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    @Override
    public ModuleHandler getHandler(Module module, String ruleUID) {
        ModuleHandler handler = handlers.get(ruleUID + module.getId());
        if (handler == null) {
            handler = internalCreate(module, ruleUID);
            if (handler != null) {
                handlers.put(ruleUID + module.getId(), handler);
            }
        }
        return handler;
    }

    /**
     * Create a new handler for the given module.
     *
     * @param module the {@link Module} for which a handler shoult be created
     * @param ruleUID the id of the rule for which the handler should be created
     * @return A {@link ModuleHandler} instance or <code>null</code> if thins module type is not supported
     */
    protected abstract @Nullable ModuleHandler internalCreate(Module module, String ruleUID);

    public void dispose() {
        for (ModuleHandler handler : handlers.values()) {
            if (handler != null) {
                handler.dispose();
            }
        }
        handlers.clear();
    }

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler hdlr) {
        ModuleHandler handler = handlers.get(ruleUID + module.getId());
        if (handler != null) {
            this.handlers.remove(ruleUID + module.getId());
            if (!this.handlers.containsValue(hdlr)) {
                handler.dispose();
                handler = null;
            }
        }

    }
}
