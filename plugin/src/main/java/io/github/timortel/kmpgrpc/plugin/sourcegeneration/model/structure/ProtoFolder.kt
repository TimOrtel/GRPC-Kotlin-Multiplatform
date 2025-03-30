package io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.structure

import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.file.ProtoFile
import io.github.timortel.kmpgrpc.plugin.sourcegeneration.model.ProtoProject

data class ProtoFolder(
    val name: String,
    val folders: List<ProtoFolder>,
    val files: List<ProtoFile>
) {
    lateinit var parent: Parent

    /**
     * The path relative to the root dir. Null for the root dir
     */
    val path: String?
        get() = when (val p = parent) {
            is Parent.Project -> null
            is Parent.Folder -> {
                val parentPath = p.folder.path
                if (parentPath == null) {
                    name
                } else {
                    "$parentPath/$name"
                }
            }
        }

    val project: ProtoProject
        get() = when (val p = parent) {
            is Parent.Folder -> p.folder.project
            is Parent.Project -> p.project
        }

    init {
        folders.forEach { it.parent = Parent.Folder(this) }
        files.forEach { it.folder = this }
    }

    /**
     * Tries to resolve the import.
     * @return the file associated with the path, or null if not found.
     */
    fun resolveImport(path: String): ProtoFile? {
        if (path.contains("/")) {
            // the file must be in a subdirectory
            val folderName = path.substringBefore('/')
            val remainingPath = path.substringAfter('/')

            val folder = folders.firstOrNull { it.name == folderName } ?: return null
            return folder.resolveImport(remainingPath)
        } else {
            // the file must be in this folder
            return files.firstOrNull { it.fileName == path }
        }
    }

    /**
     * Collect all files from this directory and its subdirectories
     */
    fun collectFiles(): List<ProtoFile> {
        return files + folders.flatMap { it.collectFiles() }
    }

    sealed interface Parent {
        data class Folder(val folder: ProtoFolder) : Parent
        data class Project(val project: ProtoProject) : Parent
    }
}
