/*
 * Copyright 2019 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.persistence

import com.arangodb.async.{ArangoCursorAsync, ArangoDatabaseAsync}
import com.arangodb.model.AqlQueryOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._

object ArangoImplicits {

    implicit class ArangoDatabaseAsyncScalaWrapper(db: ArangoDatabaseAsync) {
      def queryOne[T: Manifest](queryString: String,
                                bindVars: Map[String, AnyRef] = Map.empty,
                                options: AqlQueryOptions = null)
                               (implicit ec: ExecutionContext): Future[T] = {
        for (
          res <- queryAs[T](queryString, bindVars, options)
          if res.hasNext
        ) yield res.next
      }

      def queryStream[T: Manifest](queryString: String,
                                bindVars: Map[String, AnyRef] = Map.empty,
                                options: AqlQueryOptions = null)
                               (implicit ec: ExecutionContext): Future[Stream[T]] = {
        queryAs[T](queryString, bindVars, options)
          .map(_.iterator().asScala.toStream)
      }

      def queryAs[T: Manifest](queryString: String,
                             bindVars: Map[String, AnyRef] = Map.empty,
                             options: AqlQueryOptions = null
                            ): Future[ArangoCursorAsync[T]] = {
        val resultType = implicitly[Manifest[T]].runtimeClass.asInstanceOf[Class[T]]
        db.query(queryString, bindVars.asJava, options, resultType).toScala
      }
    }

}
