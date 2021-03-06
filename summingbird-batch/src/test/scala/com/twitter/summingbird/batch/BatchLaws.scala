/*
Copyright 2013 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.twitter.summingbird.batch

import org.scalacheck.{ Arbitrary, Properties }
import org.scalacheck.Prop._

import java.util.Date
import java.util.concurrent.TimeUnit

import com.twitter.algebird.Interval

object BatchLaws extends Properties("BatchID") {
  import Generators._

  property("BatchIDs should RT to String") =
    forAll { batch: BatchID => batch == BatchID(batch.toString) }

  property("BatchID should parse strings as expected") =
    forAll { l: Long => (BatchID("BatchID." + l)) == BatchID(l) }

  property("BatchID should respect ordering") =
    forAll { (a: Long, b: Long) =>
      a.compare(b) == BatchID(a).compare(BatchID(b))
    }

  property("BatchID should respect addition and subtraction") =
    forAll { (init: Long, forward: Long, backward: Long) =>
      val batchID = BatchID(init)
      (batchID + forward - backward) == batchID + (forward - backward)
    }

  property("BatchID should roll forward and backward") =
    forAll { (b: Long) =>
      BatchID(b).next.prev == BatchID(b) &&
      BatchID(b).prev.next == BatchID(b) &&
      BatchID(b).prev == BatchID(b - 1L)
    }

  property("range, toInterval and toIterable should be equivalent") =
    forAll { (b1: BatchID, diff: SmallLong) =>
      val b2 = b1 + (diff.get)
      val interval = Interval.leftClosedRightOpen(b1, b2.next)
      (BatchID.toInterval(BatchID.range(b1, b2)) == Some(interval)) &&
        BatchID.toIterable(interval).toList == BatchID.range(b1, b2).toList
    }
}
