package com.danielpuiu.xjc;

import com.danielpuiu.annotations.Remove;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created: 16/08/2017 Author:  dpuiu
 */
public class RemovePlugin extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(RemovePlugin.class);

    public String getOptionName() {
        return "Xremove";
    }

    public String getUsage() {
        return "-Xremove: xjc remove";
    }

    public boolean run(Outline outline, Options options, ErrorHandler errorHandler) throws SAXException {
        for (final ClassOutline classOutline: outline.getClasses()) {
            List<JFieldVar> toRemove = classOutline.implClass.fields().values().stream().filter(this::hasRemoveAnnotation).collect(Collectors.toList());
            toRemove.forEach(f -> removeField(classOutline, f));
        }

        return true;
    }

    private boolean hasRemoveAnnotation(JFieldVar jFieldVar) {
        return jFieldVar.annotations().stream().map(a -> a.getAnnotationClass().fullName()).anyMatch(Remove.class.getName()::equals);
    }

    private void removeField(ClassOutline classOutline, JFieldVar jFieldVar) {
        String propertyName = jFieldVar.name();
        LOG.debug("Remove property {}", propertyName);

        JDefinedClass jDefinedClass = classOutline.implClass;
        jDefinedClass.removeField(jFieldVar);

        final List<String> ignoredMethodNames = getIgnoredMethodNames(propertyName);
        LOG.debug("Remove getter/setter {}", ignoredMethodNames);

        Collection<JMethod> methods = jDefinedClass.methods();
        LOG.debug("Got class methods");

        List<JMethod> ignoredMethods = methods.stream().filter(m -> ignoredMethodNames.contains(m.name())).collect(Collectors.toList());
        LOG.debug("Got ignored methods");

        methods.removeAll(ignoredMethods);
        LOG.debug("Removed methods from class");
    }

    private List<String> getIgnoredMethodNames(String propertyName) {
        // capitalize
        String capitalizedPropertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

        List<String> ignoredMethodNames = new ArrayList<>();
        ignoredMethodNames.add("get" + capitalizedPropertyName);
        ignoredMethodNames.add("set" + capitalizedPropertyName);

        return ignoredMethodNames;
    }
}

