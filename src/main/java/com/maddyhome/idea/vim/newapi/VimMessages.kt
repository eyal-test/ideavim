/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.newapi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.maddyhome.idea.vim.injector
import com.maddyhome.idea.vim.vimscript.services.OptionConstants
import com.maddyhome.idea.vim.vimscript.services.OptionService
import java.awt.Toolkit

interface VimMessages {
  fun showMessage(message: String?)
  fun indicateError()
  fun clearError()
}

class IjVimMessages : VimMessages {

  private var message: String? = null
  private var error = false
  private var lastBeepTimeMillis = 0L

  override fun showMessage(msg: String?) {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      message = msg
    }
    val pm = ProjectManager.getInstance()
    val projects = pm.openProjects
    for (project in projects) {
      val bar = WindowManager.getInstance().getStatusBar(project)
      if (bar != null) {
        if (msg.isNullOrEmpty()) {
          bar.info = ""
        } else {
          bar.info = "VIM - $msg"
        }
      }
    }
  }

  override fun indicateError() {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      error = true
    } else if (!injector.optionService.isSet(
        OptionService.Scope.GLOBAL,
        OptionConstants.visualbellName,
        OptionConstants.visualbellName
      )
    ) {
      // Vim only allows a beep once every half second - :help 'visualbell'
      val currentTimeMillis = System.currentTimeMillis()
      if (currentTimeMillis - lastBeepTimeMillis > 500) {
        Toolkit.getDefaultToolkit().beep()
        lastBeepTimeMillis = currentTimeMillis
      }
    }
  }

  override fun clearError() {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      error = false
    }
  }
}
