package com.loror.compiler;

import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ItemClick;
import com.loror.lororUtil.view.ItemLongClick;
import com.loror.lororUtil.view.LongClick;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class ElementInfo {

    private ProcessingEnvironment processingEnv;
    private TypeElement typeElement;
    private LinkedList<ElementInfoItem> elementInfoItems = new LinkedList<>();

    private String packageName;
    private String className;
    private String proxyClassName;

    private final String appPackageName = null;//app包名,请配置避免生成反射代码

    public ElementInfo(ProcessingEnvironment processingEnv, TypeElement typeElement) {
        this.processingEnv = processingEnv;
        this.typeElement = typeElement;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
        this.packageName = packageElement.getQualifiedName().toString();
        this.className = ClassValidator.getClassName(typeElement, packageName);
        this.proxyClassName = className + "$$Finder";
    }

    public void addElement(int type, Element variableElement) {
        switch (type) {
            case 0:
                elementInfoItems.add(new ElementInfoItem(type,
                        variableElement.asType().toString(),
                        variableElement.getSimpleName().toString(),
                        variableElement.getAnnotation(Find.class).value(),
                        0,
                        0));
                break;
            case 1:
                for (int i = 0; i < variableElement.getAnnotation(Click.class).id().length; i++) {
                    elementInfoItems.add(new ElementInfoItem(type,
                            variableElement.asType().toString(),
                            variableElement.getSimpleName().toString(),
                            variableElement.getAnnotation(Click.class).id()[i],
                            variableElement.getAnnotation(Click.class).clickSpace(),
                            variableElement.toString().endsWith("()") ? 0 : 1));
                }
                break;
            case 2:
                elementInfoItems.add(new ElementInfoItem(type,
                        variableElement.asType().toString(),
                        variableElement.getSimpleName().toString(),
                        variableElement.getAnnotation(ItemClick.class).id(),
                        variableElement.getAnnotation(ItemClick.class).clickSpace(),
                        variableElement.toString().endsWith("()") ? 0 : 1));
                break;
            case 3:
                for (int i = 0; i < variableElement.getAnnotation(LongClick.class).id().length; i++) {
                    elementInfoItems.add(new ElementInfoItem(type,
                            variableElement.asType().toString(),
                            variableElement.getSimpleName().toString(),
                            variableElement.getAnnotation(LongClick.class).id()[i],
                            0,
                            variableElement.toString().endsWith("()") ? 0 : 1));
                }
                break;
            case 4:
                elementInfoItems.add(new ElementInfoItem(type, variableElement.asType().toString(),
                        variableElement.getSimpleName().toString(),
                        variableElement.getAnnotation(ItemLongClick.class).id(),
                        0,
                        variableElement.toString().endsWith("()") ? 0 : 1));
                break;
        }
    }

    public void generateSource() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("// Generated code. Do not modify!\n");
            builder.append("package ").append(packageName).append(";\n\n");
            builder.append("public class ").append(proxyClassName.replace(".", "$")).append(" implements com.loror.lororUtil.view.ClassAnotationFinder, android.view.View.OnClickListener, android.view.View.OnLongClickListener{\n");
            builder.append("private Object holder;\n");
            builder.append("@Override\n");
            builder.append("public void find(Object holder, Object source) {\n");
            builder.append("int id = 0;\n");
            if (appPackageName == null) {
                builder.append("Class<?> rClass = null;\n");
            }
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
                    if (appPackageName == null) {
                        builder.append("id = 0;\n");
                        builder.append("try{\n");
                        builder.append("if(rClass == null){\n");
                        builder.append("android.content.Context context = source instanceof android.app.Activity ? (android.content.Context) source : source instanceof android.app.Fragment ? ((android.app.Fragment) source).getActivity() : source instanceof android.support.v4.app.Fragment ? ((android.support.v4.app.Fragment) source).getActivity() : source instanceof android.app.Dialog ? ((android.app.Dialog) source).getContext() : ((android.view.View) source).getContext();\n");
                        builder.append("rClass = Class.forName(context.getPackageName() + \".R$id\");\n");
                        builder.append("}\n");
                        builder.append("java.lang.reflect.Field idField = rClass.getDeclaredField(\"").append(elementInfoItem.valueName).append("\");\n");
                        builder.append("id = idField.getInt(idField);\n");
                        builder.append("}catch(Exception e){\n");
                        builder.append("e.printStackTrace();\n}");
                    } else {
                        builder.append("id = ").append(appPackageName).append(".R.id.").append(elementInfoItem.valueName).append(";\n");
                    }
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

            List<ElementInfoItem> clicks = new LinkedList<>();
            List<ElementInfoItem> longClicks = new LinkedList<>();

            builder.append("@Override\n");
            builder.append("public void click(final Object holder, Object source) {\n");
            builder.append("this.holder = holder;\n");
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
                        clicks.add(elementInfoItem);
                        builder.append("view.setOnClickListener(this);\n");
                    } else if (elementInfoItem.type == 2) {
                        if (elementInfoItem.id != 0) {
                            builder.append("if(view instanceof android.widget.AbsListView){\n");
                            builder.append("((android.widget.AbsListView)view).setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {\n");
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
                            builder.append("((com.loror.lororUtil.view.ItemClickAble)view).setOnItemClickListener(new com.loror.lororUtil.view.OnItemClickListener() {\n");
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
                    } else if (elementInfoItem.type == 3) {
                        longClicks.add(elementInfoItem);
                        builder.append("view.setOnLongClickListener(this);\n");
                    } else if (elementInfoItem.type == 4) {
                        if (elementInfoItem.id != 0) {
                            builder.append("if(view instanceof android.widget.AbsListView){\n");
                            builder.append("((android.widget.AbsListView)view).setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {\n");
                            builder.append("@Override\n");
                            builder.append("public boolean onItemLongClick(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {\n");
                            builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                                    .append("(view, position);\n");
                            builder.append("return true;\n");
                            builder.append("}\n");
                            builder.append("});\n");
                            builder.append("}else if(view instanceof com.loror.lororUtil.view.ItemLongClickAble){\n");
                            builder.append("((com.loror.lororUtil.view.ItemLongClickAble)view).setOnItemLongClickListener(new com.loror.lororUtil.view.OnItemClickListener() {\n");
                            builder.append("@Override\n");
                            builder.append("public void onItemClick(android.view.View view, int position) {\n");
                            builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName)
                                    .append("(view, position);\n");
                            builder.append("}\n");
                            builder.append("});\n");
                            builder.append("}\n");
                        }
                    }
                    builder.append("}\n");
                }
                if (isZero) {
                    if (elementInfoItem.type == 2) {
                        builder.append("if(source instanceof android.view.View){\n");
                        builder.append("final long clickSpace = ").append(elementInfoItem.clickSpace).append(";\n");
                        builder.append("((android.view.View)source).setOnClickListener(new android.view.View.OnClickListener() {\n");
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
                        builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName);
                        if (elementInfoItem.param == 0) {
                            builder.append("();\n");
                        } else {
                            builder.append("(v);\n");
                        }
                        builder.append("}\n");
                        builder.append("});\n");
                        builder.append("}\n");
                    } else if (elementInfoItem.type == 4) {
                        builder.append("if(source instanceof android.view.View){\n");
                        builder.append("final long clickSpace = ").append(elementInfoItem.clickSpace).append(";\n");
                        builder.append("((android.view.View)source).setOnLongClickListener(new android.view.View.OnLongClickListener() {\n");
                        builder.append("@Override\n");
                        builder.append("public boolean onLongClick(android.view.View view) {\n");
                        builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfoItem.valueName);
                        if (elementInfoItem.param == 0) {
                            builder.append("();\n");
                        } else {
                            builder.append("(v);\n");
                        }
                        builder.append("return true;\n");
                        builder.append("}\n");
                        builder.append("});\n");
                        builder.append("}\n");
                    }
                }
            }
            builder.append("android.util.Log.e(\"TAG_\",\"").append(packageName).append(".").append(className).append(" clicked\");\n");
            builder.append("}\n");

            if (clicks.size() > 0) {
                int size = 0;
                for (ElementInfoItem elementInfo : clicks) {
                    if (elementInfo.clickSpace > 0) {
                        size++;
                    }
                }
                if (size > 0) {
                    builder.append("private long[] clickTimes = new long[").append(size).append("];\n");
                }
            }
            builder.append("@Override\n");
            builder.append("public void onClick(android.view.View v) {\n");
            if (clicks.size() > 0) {
                builder.append("switch (v.getId()){\n");
                int index = 0;
                for (ElementInfoItem elementInfo : clicks) {
                    builder.append("case ").append(elementInfo.id).append(":\n");
                    if (elementInfo.clickSpace > 0) {
                        builder.append("if (System.currentTimeMillis() - clickTimes[").append(index++).append("] > ").append(elementInfo.clickSpace).append(") {\n");
                        builder.append("clickTimes[0] = System.currentTimeMillis();\n");
                    }
                    builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfo.valueName);
                    if (elementInfo.param == 0) {
                        builder.append("();\n");
                    } else {
                        builder.append("(v);\n");
                    }
                    if (elementInfo.clickSpace > 0) {
                        builder.append("}\n");
                    }
                    builder.append("break;\n");
                }
                builder.append("}\n");
            }
            builder.append("}\n");

            builder.append("@Override\n");
            builder.append("public boolean onLongClick(android.view.View v) {\n");
            if (longClicks.size() > 0) {
                builder.append("switch (v.getId()){\n");
                for (ElementInfoItem elementInfo : longClicks) {
                    builder.append("case ").append(elementInfo.id).append(":\n");
                    builder.append("((").append(packageName).append(".").append(className).append(")holder)").append(".").append(elementInfo.valueName);
                    if (elementInfo.param == 0) {
                        builder.append("();\n");
                    } else {
                        builder.append("(v);\n");
                    }
                    builder.append("break;\n");
                }
                builder.append("}\n");
            }
            builder.append("return true;\n");
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
        int param;

        public ElementInfoItem(int type, String classType, String valueName, int id, long clickSpace, int param) {
            this.type = type;
            this.classType = classType;
            this.valueName = valueName;
            this.id = id <= 0 ? 0 : id;
            this.clickSpace = clickSpace;
            this.param = param;
        }
    }
}
