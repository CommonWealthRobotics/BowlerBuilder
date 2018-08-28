package com.neuronrobotics.bowlerbuilder.view.util

import javafx.beans.value.ChangeListener
import javafx.scene.control.TreeItem
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHRepository

object GitHubRepoFileTree {

    /**
     * Generate a [TreeItem] for each [GHRepository] under the owner's name.
     */
    @JvmStatic
    fun getTreeItemForUser(ownerName: String, repos: Collection<GHRepository>): TreeItem<String> {
        return TreeItem(ownerName).apply {
            children.setAll(repos.map { getTreeItemForRepo(it) })
        }
    }

    /**
     * Generate a [TreeItem] for a [GHRepository] containing the repo name and all of its contents.
     */
    @JvmStatic
    fun getTreeItemForRepo(repo: GHRepository): TreeItem<String> {
        val repoItem = TreeItem(repo.name)

        // Temporary item so repoItem has a dropdown arrow
        repoItem.children.add(TreeItem())

        repoItem.expandedProperty().addListener { _, _, new ->
            if (new) {
                repo.getDirectoryContent(".").map {
                    getTreeItemForRepoContent(repo, it, null, null)
                }.let { repoItem.children.setAll(it) }
            }
        }

        return repoItem
    }

    /**
     * Recursively generate a [TreeItem] instances, optionally simplifying empty directories.
     */
    private fun getTreeItemForRepoContent(
        rootRepo: GHRepository,
        content: GHContent,
        parentItem: TreeItem<String>?,
        parentContent: GHContent?
    ): TreeItem<String> {
        val contentItem = TreeItem(content.name)

        if (content.isDirectory) {
            // Temporary item so contentItem has a dropdown arrow
            contentItem.children.add(TreeItem())

            var listener: ChangeListener<Boolean>? = null

            listener = ChangeListener { _, _, new ->
                if (new) {
                    val directoryContent = content.listDirectoryContent().asList()
                    if (parentItem != null && canSimplifyDirectory(parentContent)) {
                        parentItem.value = "${parentItem.value}.${content.name}"
                        directoryContent.map {
                            /**
                             * We need to keep the parentItem the same as last time so the
                             * TreeItems get added to the same TreeItem (thereby extending the name
                             * of the same TreeItem); however, the content for the next item needs
                             * to be the current content so the correct directory is checked
                             * for emptiness.
                             */
                            getTreeItemForRepoContent(rootRepo, it, parentItem, content)
                        }.let { parentItem.children.setAll(it) }
                    } else {
                        directoryContent.map {
                            getTreeItemForRepoContent(rootRepo, it, contentItem, content)
                        }.let { contentItem.children.setAll(it) }
                    }

                    /**
                     * Remove the listener so it won't re-expand incorrectly. If the user wants
                     * to reload the files, they should reload the git menus.
                     */
                    contentItem.expandedProperty().removeListener(listener)
                }
            }

            contentItem.expandedProperty().addListener(listener)
        }

        return contentItem
    }

    /**
     * Whether a directory contains one item and therefore can be combined with the parent
     * directory.
     */
    private fun canSimplifyDirectory(parentContent: GHContent?) =
            parentContent != null && parentContent.listDirectoryContent().asList().size == 1
}
