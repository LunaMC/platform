/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.lunamc.platform.internal.plugin.annotation.processor;

import com.github.zafarkhaja.semver.Version;
import io.lunamc.platform.plugin.Plugin;
import io.lunamc.platform.plugin.annotation.LunaPlugin;
import io.lunamc.platform.utils.XMLUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({ "io.lunamc.platform.plugin.annotation.LunaPlugin" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({ LunaPluginAnnotationProcessor.OPTION_OUTPUT_FILE, LunaPluginAnnotationProcessor.OPTION_SKIP_XML_VALIDATION })
public class LunaPluginAnnotationProcessor extends AbstractProcessor {

    public static final String OPTION_OUTPUT_FILE = "io.lunamc.platform.preprocessor.providedPluginsOutputFile";
    public static final String OPTION_SKIP_XML_VALIDATION = "io.lunamc.platform.preprocessor.skipXmlValidation";
    private static final String PLUGIN_CLASS = Plugin.class.getName();
    private static final String DEFAULT_OUTPUT_FILE = "LUNAMC-RESOURCES/providedPlugins.xml";

    private final List<ProvidedPlugins.ProvidedPlugin> providedPlugins = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        providedPlugins.clear();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.errorRaised())
            return false;

        if (roundEnv.processingOver()) {
            savePlugins();
            return false;
        }

        Messager messager = processingEnv.getMessager();
        TypeMirror pluginTypeMirror = processingEnv.getElementUtils().getTypeElement(PLUGIN_CLASS).asType();
        for (Element element : roundEnv.getElementsAnnotatedWith(LunaPlugin.class)) {
            if (!element.getKind().isClass()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Element must be a class", element);
                continue;
            }

            if (!processingEnv.getTypeUtils().isAssignable(element.asType(), pluginTypeMirror)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Element must inherit from " + PLUGIN_CLASS, element);
                continue;
            }

            LunaPlugin pluginAnnotation = element.getAnnotation(LunaPlugin.class);
            ProvidedPlugins.ProvidedPlugin providedPlugin = ProvidedPlugins.ProvidedPlugin.create(
                    pluginAnnotation.id(),
                    Version.valueOf(pluginAnnotation.version()).toString(),
                    getName(element).toString(),
                    Arrays.stream(pluginAnnotation.pluginDependencies())
                            .map(pluginDependency -> ProvidedPlugins.PluginDependency.create(
                                    pluginDependency.id(),
                                    pluginDependency.versionExpression()
                            ))
                            .collect(Collectors.toList())
            );
            messager.printMessage(Diagnostic.Kind.NOTE, "Plugin found: " + providedPlugin, element);
            providedPlugins.add(providedPlugin);
        }

        return false;
    }

    private void savePlugins() {
        ProvidedPlugins result = new ProvidedPlugins();
        result.setProvidedPlugins(new ArrayList<>(providedPlugins));
        try (OutputStream out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", getOutputFile()).openOutputStream()) {
            JAXBContext context = JAXBContext.newInstance(ProvidedPlugins.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            if (!isSkipXmlValidation())
                marshaller.setSchema(XMLUtils.createSchema("http://static.lunamc.io/xsd/provided-plugin-1.0.xsd"));
            marshaller.marshal(result, out);
        } catch (IOException | JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private String getOutputFile() {
        return processingEnv.getOptions().getOrDefault(OPTION_OUTPUT_FILE, DEFAULT_OUTPUT_FILE);
    }

    private boolean isSkipXmlValidation() {
        return Boolean.parseBoolean(processingEnv.getOptions().get(OPTION_SKIP_XML_VALIDATION));
    }

    private static Name getName(Element element) {
        return element instanceof QualifiedNameable ? ((QualifiedNameable) element).getQualifiedName() : element.getSimpleName();
    }
}
