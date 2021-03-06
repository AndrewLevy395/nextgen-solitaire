package org.combinators.solitaire.beta

import com.github.javaparser.ast.CompilationUnit
import javax.inject.Inject
import org.combinators.cls.git._
import org.combinators.cls.interpreter.ReflectedRepository
import org.combinators.cls.types.Constructor
import org.combinators.solitaire.shared.cls.Synthesizer
import org.combinators.templating.persistable.JavaPersistable._
import org.webjars.play.WebJarsUtil
import play.api.inject.ApplicationLifecycle

/** Loads and runs the combinators to generate the Beta variation.
  *
  * @param webJars
  */
class Beta @Inject()(webJars: WebJarsUtil, applicationLifecycle: ApplicationLifecycle) extends InhabitationController(webJars, applicationLifecycle) with RoutingEntries {

  val solitaire = beta// new domain.beta.Domain()

  lazy val repository = new BetaDomain(solitaire) with controllers {}

  lazy val Gamma = repository.init(ReflectedRepository(repository, classLoader = this.getClass.getClassLoader), solitaire)


  lazy val combinatorComponents = Gamma.combinatorComponents

  lazy val targets: Seq[Constructor] = Synthesizer.allTargets(solitaire)

  lazy val results: Results =
    EmptyInhabitationBatchJobResults(Gamma).addJobs[CompilationUnit](targets).compute()

  lazy val controllerAddress: String = solitaire.name.toLowerCase

}
