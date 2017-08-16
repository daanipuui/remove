# remove
xjc remove property from generated class

To remove a property from the generated class you must follow these steps:
1. Add this plugin to the org.jvnet.jaxb2.maven plugin.
2. Add -Xremove to the list of arguments.
3. Bind the property you want to remove with the remove annotation:

    <jxb:bindings node="...">
        <annox:annotate target="field">@com.danielpuiu.annotations.Remove</annox:annotate>
    </jxb:bindings>
