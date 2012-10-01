
package axle

/**
 * Enough Haskell in Scala to be able to follow along with "Learn You a Haskell for Great Good" in Axle/Scala
 *
 * Implementation details:
 *
 * If we define intersperse with a single argument list we would have to use
 *
 *    (intersperse[Char] _ curried) apply('-')
 *
 * instead of simply
 *
 *     intersperse('-')
 */

object LearnYouAnAxle {

  object VariousFunctions {

    // TODO: merge fmap and fmap2

    // "explicit" fmap functions, which require the caller to specify the
    // FunctorN.*Functor typeclass that provides the implementation of fmap

    def fmapexp[M[_], A, B](f: A => B, functor: M[A], tc: Functor[M]): M[B] = tc.fmap(f, functor)

    def fmap2exp[M[_, _], T, A, B](f: A => B, functor: M[T, A], tc: Functor2[M]): M[T, B] = tc.fmap(f, functor)

    // "implicit" fmaps, which optionally allow Scala to determine the 
    // FunctorN.*Functor typeclass that provides the implementation of fmap

    // def fmap[M[_], A, B](f: A => B, functor: M[A])(implicit tc: Functor[M]): M[B] = tc.fmap(f, functor)

    // def fmap2[M[_,_], T, A, B](f: A => B, functor: M[T,A])(implicit tc: Functor2[M]): M[T,B] = tc.fmap(f, functor)

    // These versions of the implicit fmaps use the "implicitly" lookup instead of 
    // placing it in an implicit parameter group.  This is available as of Scala 2.8

    def fmap[M[_]: Functor, A, B](f: A => B, functor: M[A]): M[B] = implicitly[Functor[M]].fmap(f, functor)

    def fmap2[M[_, _]: Functor2, T, A, B](f: A => B, functor: M[T, A]): M[T, B] =  implicitly[Functor2[M]].fmap(f, functor)


    // Functor laws:

    // Law 1: fmap id = id

    def functorLaw1a[M[_]: Functor, A](functor: M[A]): Boolean = {
      val lhs = fmap(id[A], functor)
      val rhs = id[M[A]](functor)
      println("lhs = " + lhs)
      println("rhs = " + rhs)
      val ok = lhs == rhs
      println("equal? " + ok)
      ok
    }

    def functorLaw1b[M[_, _]: Functor2, A, B](functor: M[A, B]): Boolean = {
      val lhs = fmap2(id[B], functor)
      val rhs = id[M[A, B]](functor)
      println("lhs = " + lhs)
      println("rhs = " + rhs)
      val ok = lhs == rhs
      println("equal? " + ok)
      ok
    }

    // Law 2: fmap (f . g) = fmap f . fmap g
    //   aka: forall functors F: fmap (f . g) F = fmap f (fmap g F)

    // def fmap[T, A, B](f: A => B, functor: Function1[T, A]): Function1[T, B] = f.compose(functor)

    /*
    val lhs = f.compose(g).compose(functor)
    val rhs = f.compose(g.compose(functor))

    functor;(g;f)
    (functor;g);f
*/

    def functorLaw2a[M[_]: Functor, A, B, C](f: (B) => C, g: (A) => B, functor: M[A]): Boolean = {
      // val fog: Function1[A, C] = f.compose(g)
      val lhs = fmap(f.compose(g), functor)
      val rhs = fmap(f, fmap(g, functor))
      val ok = lhs == rhs
      println("lhs = " + lhs)
      println("rhs = " + rhs)
      ok
    }

    def functorLaw2b[M[_, _]: Functor2, T, A, B, C](f: (B) => C, g: (A) => B, functor: M[T, A]): Boolean = {
      val fog: Function1[A, C] = f.compose(g)
      val lhs = fmap2(fog, functor)
      val rhs = fmap2(f, fmap2(g, functor))
      val ok = lhs == rhs
      println("lhs = " + lhs)
      println("rhs = " + rhs)
      ok
    }

    // Applicative Functor stuff
    /*
    def pure[M[_] : Applicative, A](a: A): M[A] = implicitly[Applicative[M]].pure(a)

    def pure2[M[_, _] : Applicative2, T, A](a: A): M[T, A] = implicitly[Applicative2[M]].pure(a)

    def pure3[M[_, _, _] : Applicative3, T, U, A](a: A): M[T, U, A] = implicitly[Applicative3[M]].pure(a)
    */
  }

  // Functor traits and implicit objects

  trait Functor[F[_]] {
    def fmap[A, B](f: A => B, functor: F[A]): F[B]
  }

  object Functor {

    implicit object OptionFunctor extends Functor[Option] {
      def fmap[A, B](f: A => B, functor: Option[A]): Option[B] = functor.map(f)
    }

    implicit object ListFunctor extends Functor[List] {
      def fmap[A, B](f: A => B, functor: List[A]): List[B] = functor.map(f)
    }

    implicit object Function0Functor extends Functor[Function0] {
      def fmap[A, B](f: A => B, functor: Function0[A]): Function0[B] =
        (() => f(functor())).asInstanceOf[Function0[B]] // ?? f.compose(functor)
    }
  }

  trait Functor2[F[_, _]] {
    def fmap[T, A, B](f: A => B, functor: F[T, A]): F[T, B]
  }

  object Functor2 {

    implicit object EitherFunctor extends Functor2[Either] {
      def fmap[T, A, B](f: A => B, functor: Either[T, A]): Either[T, B] = functor match {
        case Right(v) => Right(f(v))
        case Left(v) => Left(v)
      }
    }

    implicit object Function1Functor extends Functor2[Function1] {
      def fmap[T, A, B](f: A => B, functor: Function1[T, A]): Function1[T, B] = f.compose(functor)
    }

  }

  // Applicative traits and implicit objects

  // TODO: create an implicit "enrichment" for Option (for starters)
  //   def enApplicativeOption ...
  // that will allow me to embue Option[(A) => B] with the <*> method
  /*
  trait Applicative[M[_]] {
    def pure[A](a: A): M[A]
    def <*>[A, B](something: X): M[B] = fOpt match {
      case Some(f) => fmap(f, something)
      case None    => None
    }
  }

  object Applicative {
    // TODO: how would this "pure" route to List vs Some ?
    implicit object ListApplicative extends Applicative[List] {
      def pure[A](a: A) = List(a)
    }
    implicit object OptionApplicative extends Applicative[Option] {
      def pure[A](a: A) = Some(a)
    }
  }

  trait Applicative2[M[_,_]] {
    def pure[T, A](a: A): M[T,A]
    // def <*>[T, A, B](f: A => B, applicative: M[T, A]): M[T, B]
  }

  object Applicative2 {
    implicit object Function1Applicative extends Applicative2[Function1] {
      def pure[T, A](a: A) = (t: T) => a // TODO
    }
  }

  trait Applicative3[M[_,_,_]] {
    def pure[T, U, A](a: A): M[T,U,A]
    // def <*>[T, U, A, B](f: A => B, applicative: M[T, U, A]): M[T, U, B]
  }

  object Applicative3 {
    implicit object Function2Applicative extends Applicative3[Function2] {
      def pure[T, U, A](a: A) = (t: T, u: U) => a // TODO
     }
  }
  */
}

