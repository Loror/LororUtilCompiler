package com.loror.compiler;


import com.loror.lororUtil.view.Find;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class FinderProcessor extends AbstractProcessor {

    private HashMap<String, ElementInfo> elementInfoHashMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        elementInfoHashMap.clear();
        for (Element element : roundEnv.getElementsAnnotatedWith(Find.class)) {
            VariableElement variableElement = (VariableElement) element;
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            String fqClassName = typeElement.getQualifiedName().toString();
            ElementInfo elementInfo = elementInfoHashMap.get(fqClassName);
            if (elementInfo == null) {
                elementInfo = new ElementInfo(processingEnv,typeElement);
                elementInfoHashMap.put(fqClassName, elementInfo);
            }
            elementInfo.addElement(variableElement);
        }
        for (String key : elementInfoHashMap.keySet()) {
            ElementInfo elementInfo = elementInfoHashMap.get(key);
            elementInfo.generateSource();
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(Find.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
