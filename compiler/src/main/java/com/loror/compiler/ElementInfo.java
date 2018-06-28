package com.loror.compiler;

import com.loror.finder.Find;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

public class ElementInfo {

    private ProcessingEnvironment processingEnv;
    private TypeElement typeElement;
    private LinkedList<ElementInfoItem> elementInfoItems = new LinkedList<>();

    private String packageName;
    private String className;
    private String proxyClassName;

    public ElementInfo(ProcessingEnvironment processingEnv, TypeElement typeElement) {
        this.processingEnv = processingEnv;
        this.typeElement = typeElement;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
        this.packageName = packageElement.getQualifiedName().toString();
        this.className = ClassValidator.getClassName(typeElement, packageName);
        this.proxyClassName = className + "$$Finder";
    }

    public void addElement(VariableElement variableElement) {
        elementInfoItems.add(new ElementInfoItem(variableElement.asType().toString(), variableElement.getSimpleName().toString(), variableElement.getAnnotation(Find.class).value()));
    }

    public void generateSource() {
        String name = typeElement.getQualifiedName().toString();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("// Generated code. Do not modify!\n");
            builder.append("package ").append(packageName).append(";\n\n");
            builder.append("public class ").append(proxyClassName).append(" implements com.loror.finder.Finder{\n");
            builder.append("@Override\n");
            builder.append("public void find(Object holder, Object source) {\n");
            builder.append("int id = 0;\n");
            builder.append("Class<?> rClass = null;\n");
            for (ElementInfoItem elementInfoItem : elementInfoItems) {
                boolean isZero;
                if (elementInfoItem.id != 0) {
                    isZero = false;
                    builder.append("id = ").append(elementInfoItem.id).append(";\n");
                } else {
                    isZero = true;
                    builder.append("id = 0;\n");
                    builder.append("try{\n");
                    builder.append("if(rClass == null){\n");
                    builder.append("android.content.Context context = source instanceof android.app.Activity ?(android.content.Context)source:((android.view.View)source).getContext();\n");
                    builder.append("rClass = Class.forName(context.getPackageName() + \".R$id\");\n");
                    builder.append("}\n");
                    builder.append("java.lang.reflect.Field idField = rClass.getDeclaredField(\"").append(elementInfoItem.valueName).append("\");\n");
                    builder.append("id = idField.getInt(idField);\n");
                    builder.append("}catch(Exception e){\n");
                    builder.append("e.printStackTrace();\n}");
                }
                if (isZero) {
                    builder.append("if(id != 0){\n");
                }
                builder.append("if(source instanceof android.app.Activity){\n");
                builder.append("((").append(packageName).append(".").append(className).append(")source)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.app.Activity)source).findViewById(id);\n");
                builder.append("}else if(source instanceof android.view.View){\n");
                builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.view.View)source).findViewById(id);\n");
                builder.append("}\n");
                if (isZero) {
                    builder.append("}\n");
                }
            }
            builder.append("android.util.Log.e(\"TAG_\",\"").append(packageName).append(".").append(className).append("\");\n");
            builder.append("}\n");
            builder.append("}\n");

            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    packageName + "." + proxyClassName,
                    typeElement);
            Writer writer = jfo.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProxyClassName() {
        return proxyClassName;
    }

    class ElementInfoItem {
        String classType;
        String valueName;
        int id;


        public ElementInfoItem(String classType, String valueName, int id) {
            this.classType = classType;
            this.valueName = valueName;
            this.id = id <= 0 ? 0 : id;
        }
    }
}
