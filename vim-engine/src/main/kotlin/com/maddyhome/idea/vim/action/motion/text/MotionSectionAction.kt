/*
 * Copyright 2003-2023 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */
package com.maddyhome.idea.vim.action.motion.text

import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.ImmutableVimCaret
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.api.normalizeOffset
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.CommandFlags
import com.maddyhome.idea.vim.command.MotionType
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.common.Direction
import com.maddyhome.idea.vim.handler.Motion
import com.maddyhome.idea.vim.handler.MotionActionHandler
import com.maddyhome.idea.vim.handler.toMotionOrError
import com.maddyhome.idea.vim.helper.enumSetOf
import java.util.*

public class MotionSectionBackwardEndAction : MotionSectionAction('}', Direction.BACKWARDS)
public class MotionSectionBackwardStartAction : MotionSectionAction('{', Direction.BACKWARDS)
public class MotionSectionForwardEndAction : MotionSectionAction('}', Direction.FORWARDS)
public class MotionSectionForwardStartAction : MotionSectionAction('{', Direction.FORWARDS)

public sealed class MotionSectionAction(private val charType: Char, public val direction: Direction) : MotionActionHandler.ForEachCaret() {
  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_SAVE_JUMP)

  override fun getOffset(
    editor: VimEditor,
    caret: ImmutableVimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    return getCaretToSectionMotion(
      editor,
      caret,
      charType,
      direction.toInt(),
      operatorArguments.count1,
    ).toMotionOrError()
  }

  override val motionType: MotionType = MotionType.EXCLUSIVE
}

private fun getCaretToSectionMotion(editor: VimEditor, caret: ImmutableVimCaret, type: Char, dir: Int, count: Int): Int {
  return if (caret.offset.point == 0 && count < 0 || caret.offset.point >= editor.fileSize() - 1 && count > 0) {
    -1
  } else {
    var res = injector.searchHelper.findSection(editor, caret, type, dir, count)
    if (res != -1) {
      res = editor.normalizeOffset(res, false)
    }
    res
  }
}
