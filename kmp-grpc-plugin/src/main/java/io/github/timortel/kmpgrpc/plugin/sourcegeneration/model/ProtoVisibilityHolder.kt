package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model

interface ProtoVisibilityHolder : ProtoOptionsHolder {

    val project: ProtoProject

    /**
     * The visibility of the code generated for this declaration or file.
     */
    val visibility: Visibility get() = file.project.defaultVisibility
}
