package org.combinators.solitaire.stalactites

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.{Expression, NameExpr}
import com.github.javaparser.ast.body.{BodyDeclaration}
import com.github.javaparser.ast.stmt.Statement
import de.tu_dortmund.cs.ls14.cls.interpreter.combinator
import de.tu_dortmund.cs.ls14.cls.types.{Taxonomy, Type}
import de.tu_dortmund.cs.ls14.cls.types.{Constructor}
import de.tu_dortmund.cs.ls14.cls.types.syntax._
import de.tu_dortmund.cs.ls14.twirl.Java
import org.combinators.generic
import org.combinators.solitaire.shared

trait StalactitesColumnController extends shared.Controller with generic.JavaIdioms {

	// column move designated combinators
	@combinator object ColumnControllerDef extends ColumnController ('StalactitesColumn)

	@combinator object StalactitesColumn {
		def apply(): NameExpr = {
				Java("Stalactites").nameExpression()
		}
		val semanticType: Type = 'Column('StalactitesColumn, 'ClassName)
	}

	@combinator object ColumnPressedHandler {
		def apply(): (NameExpr, NameExpr) => Seq[Statement] = {
				(widgetVariableName: NameExpr, ignoreWidgetVariableName: NameExpr) =>
				controller.column.java.ColumnPressed.render(widgetVariableName, ignoreWidgetVariableName).statements()
		}
		val semanticType: Type =
				'Pair('WidgetVariableName, 'IgnoreWidgetVariableName) =>:
					'Column('StalactitesColumn, 'Pressed) :&: 'NonEmptySeq
	}

	@combinator object ColumnClickedHandler {
		def apply(): Seq[Statement] = {
				Seq.empty
		}
		val semanticType: Type = 'Column('StalactitesColumn, 'Clicked) :&: 'NonEmptySeq
	}
	
	
  // This would be a good case of an example of 'remove a method to allow super to handle it'
	@combinator object ColumnReleasedHandler {
		def apply(): Seq[Statement] = {
				 Java("super.mouseReleased(me);").statements()
		}
		val semanticType: Type = 'Column('StalactitesColumn, 'Released) :&: 'NonEmptySeq
	}
}

// Java("super.mouseReleased(me);").statements()