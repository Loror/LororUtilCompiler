package com.loror.compiler;

import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ItemClick;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
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

    public void addElement(int type, Element variableElement) {
        elementInfoItems.add(new ElementInfoItem(type, variableElement.asType().toString(), variableElement.getSimpleName().toString(),
                type == 0 ? variableElement.getAnnotation(Find.class).value() : type == 1 ? variableElement.getAnnotation(Click.class).id() : variableElement.getAnnotation(ItemClick.class).id(),
                type == 0 ? 0 : type == 1 ? variableElement.getAnnotation(Click.class).clickSpace() : variableElement.getAnnotation(ItemClick.class).clickSpace()));
    }

    public void generateSource() {
        String name = typeElement.getQualifiedName().toString();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("// Generated code. Do not modify!\n");
            builder.append("package ").append(packageName).append(";\n\n");
            builder.append("public class ").append(proxyClassName.replace(".", "$")).append(" implements com.loror.lororUtil.view.ClassAnotationFinder{\n");
            builder.append("@Override\n");
            builder.append("public void find(Object holder, Object source) {\n");
            builder.append("int id = 0;\n");
            builder.append("Class<?> rClass = null;\n");
            for (ElementInfoItem elementInfoItem : elementInfoItems) {
                if (elementInfoItem.type != 0) {
                    continue;
                }
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
                builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.app.Activity)source).findViewById(id);\n");
                builder.append("}else if(source instanceof android.app.Fragment){\n");
                builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.app.Fragment)source).getView().findViewById(id);\n");
                builder.append("}else if(source instanceof android.support.v4.app.Fragment){\n");
                builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.support.v4.app.Fragment)source).getView().findViewById(id);\n");
                builder.append("}else if(source instanceof android.app.Dialog){\n");
                builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.app.Dialog)source).findViewById(id);\n");
                builder.append("}else if(source instanceof android.view.View){\n");
                builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                        .append(" = (").append(elementInfoItem.classType).append(")((android.view.View)source).findViewById(id);\n");
                builder.append("}\n");
                if (isZero) {
                    builder.append("}\n");
                }
            }
            builder.append("android.util.Log.e(\"TAG_\",\"").append(packageName).append(".").append(className).append(" finded\");\n");
            builder.append("}\n");
            builder.append("@Override\n");
            builder.append("public void click(final Object holder, Object source) {\n");
            builder.append("int id = 0;\n");
            builder.append("android.view.View view = null;\n");
            for (ElementInfoItem elementInfoItem : elementInfoItems) {
                if (elementInfoItem.type == 0) {
                    continue;
                }
                boolean isZero;
                if (elementInfoItem.id != 0) {
                    isZero = false;
                    builder.append("id = ").append(elementInfoItem.id).append(";\n");
                } else {
                    isZero = true;
                    builder.append("id = 0;\n");
                }
                builder.append("view = null;\n");
                if (!isZero) {
                    builder.append("if(source instanceof android.app.Activity){\n");
                    builder.append("view = ((android.app.Activity)source).findViewById(id);\n");
                    builder.append("}else if(source instanceof android.app.Fragment){\n");
                    builder.append("view = ((android.app.Fragment)source).getView().findViewById(id);\n");
                    builder.append("}else if(source instanceof android.support.v4.app.Fragment){\n");
                    builder.append("view = ((android.support.v4.app.Fragment)source).getView().findViewById(id);\n");
                    builder.append("}else if(source instanceof android.app.Dialog){\n");
                    builder.append("view = ((android.app.Dialog)source).findViewById(id);\n");
                    builder.append("}else if(source instanceof android.view.View){\n");
                    builder.append("view = ((android.view.View)source).findViewById(id);\n");
                    builder.append("}\n");
                    builder.append("if(view != null){\n");
                    if (elementInfoItem.clickSpace > 0) {
                        builder.append("final long clickSpace = ").append(elementInfoItem.clickSpace).append(";\n");
                    }
                    if (elementInfoItem.type == 1) {
                        builder.append("final android.view.View temp = view;\n");
                        builder.append("view.setOnClickListener(new android.view.View.OnClickListener() {\n");
                        if (elementInfoItem.clickSpace > 0) {
                            builder.append("private long click;\n");
                        }
                        builder.append("@Override\n");
                        builder.append("public void onClick(android.view.View v) {\n");
                        if (elementInfoItem.clickSpace > 0) {
                            builder.append("if (System.currentTimeMillis() - click < clickSpace) {\n");
                            builder.append("return;\n");
                            builder.append("}\n");
                            builder.append("click = System.currentTimeMillis();\n");
                        }
                        builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                                .append("(temp);\n");
                        builder.append("}\n");
                        builder.append("});\n");
                    } else {
                        if (elementInfoItem.id != 0) {
                            builder.append("if(view instanceof android.widget.AbsListView){\n");
                            builder.append("final android.widget.AbsListView temp = (android.widget.AbsListView)view;\n");
                            builder.append("temp.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {\n");
                            if (elementInfoItem.clickSpace > 0) {
                                builder.append("private long click;\n");
                            }
                            builder.append("@Override\n");
                            builder.append("public void onItemClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {\n");
                            if (elementInfoItem.clickSpace > 0) {
                                builder.append("if (System.currentTimeMillis() - click < clickSpace) {\n");
                                builder.append("return;\n");
                                builder.append("}\n");
                                builder.append("click = System.currentTimeMillis();\n");
                            }
                            builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                                    .append("(view, position);\n");
                            builder.append("}\n");
                            builder.append("});\n");
                            builder.append("}else if(view instanceof com.loror.lororUtil.view.ItemClickAble){\n");
                            builder.append("final com.loror.lororUtil.view.ItemClickAble temp = (com.loror.lororUtil.view.ItemClickAble)view;\n");
                            builder.append("temp.setOnItemClickListener(new com.loror.lororUtil.view.OnItemClickListener() {\n");
                            if (elementInfoItem.clickSpace > 0) {
                                builder.append("private long click;\n");
                            }
                            builder.append("@Override\n");
                            builder.append("public void onItemClick(android.view.View view, int position) {\n");
                            if (elementInfoItem.clickSpace > 0) {
                                builder.append("if (System.currentTimeMillis() - click < clickSpace) {\n");
                                builder.append("return;\n");
                                builder.append("}\n");
                                builder.append("click = System.currentTimeMillis();\n");
                            }
                            builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                                    .append("(view, position);\n");
                            builder.append("}\n");
                            builder.append("});\n");
                            builder.append("}\n");
                        }
                    }
                    builder.append("}\n");
                }
                if (isZero && elementInfoItem.type == 2) {
                    builder.append("if(source instanceof android.view.View){\n");
                    builder.append("final long clickSpace = ").append(elementInfoItem.clickSpace).append(";\n");
                    builder.append("final android.view.View temp = (android.view.View)source;\n");
                    builder.append("temp.setOnClickListener(new android.view.View.OnClickListener() {\n");
                    if (elementInfoItem.clickSpace > 0) {
                        builder.append("private long click;\n");
                    }
                    builder.append("@Override\n");
                    builder.append("public void onClick(android.view.View v) {\n");
                    if (elementInfoItem.clickSpace > 0) {
                        builder.append("if (System.currentTimeMillis() - click < clickSpace) {\n");
                        builder.append("return;\n");
                        builder.append("}\n");
                        builder.append("click = System.currentTimeMillis();\n");
                    }
                    builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                            .append("(temp);\n");
                    builder.append("}\n");
                    builder.append("});\n");
                    builder.append("}\n");
                }
            }
            builder.append("android.util.Log.e(\"TAG_\",\"").append(packageName).append(".").append(className).append(" clicked\");\n");
            builder.append("}\n");
            builder.append("}\n");

            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(
                    packageName + "." + proxyClassName.replace(".", "$"),
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
        long clickSpace;
        int type;//0,Find,1,Click,2,ItemClick


        public ElementInfoItem(int type, String classType, String valueName, int id, long clickSpace) {
            this.type = type;
            this.classType = classType;
            this.valueName = valueName;
            this.id = id <= 0 ? 0 : id;
            this.clickSpace = clickSpace;
        }
    }
}
