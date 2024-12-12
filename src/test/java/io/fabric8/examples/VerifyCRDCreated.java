package io.fabric8.examples;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

class VerifyCRDCreated {

  @Test
  void expectCrd(){
    Verify.verifyExist(Path.of("build/resources/main/multiples.sample.fabric8.io-v1.yml").toFile());
  }

}
