package com.hivemindtechnologies.sentiments.analyser

import org.specs2.mutable.Spec
import io.circe.generic.auto._
import io.circe.parser._

class CodecsSpec extends Spec {
  "Codecs" should {
    "decode an analysis" in {
      val json =
        """[{"label":"I hate you!","sentiment":{"identityAttack":{"match":false,"probability0":0.9985901713371277,"probability1":0.0014097996754571795},"insult":{"match":false,"probability0":0.9197881817817688,"probability1":0.08021184056997299},"obscene":{"match":false,"probability0":0.9980243444442749,"probability1":0.0019756436813622713},"severeToxicity":{"match":false,"probability0":0.9999986886978149,"probability1":0.0000013448078561850707},"sexualExplicit":{"match":false,"probability0":0.9994421601295471,"probability1":0.0005578756099566817},"threat":{"match":false,"probability0":0.9955812096595764,"probability1":0.004418761003762484},"toxicity":{"match":null,"probability0":0.8081976175308228,"probability1":0.19180238246917725}}}]"""
      decode[List[Analysis]](json) must beRight
    }
  }
}
