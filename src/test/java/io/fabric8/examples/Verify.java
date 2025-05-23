package io.fabric8.examples;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

class Verify {
  static void verifyExist(File file) {
    Objects.requireNonNull(file);
    if (!file.exists())
      throw new AssertionError("File " + file.getAbsoluteFile() + " does not exist");
  }

  static void verifyAbsent(File file) {
    Objects.requireNonNull(file);
    if (file.exists())
      throw new AssertionError("File " + file.getAbsoluteFile() + " exists");
  }

  static void verifyContentEquals(File file1, File file2) throws IOException {
    verifyExist(file1);
    verifyExist(file2);

    if (!Objects.equals(readFile(file1), readFile(file2))) {
      throw new AssertionError("File contents not equal: " + file1.getAbsoluteFile() + " " + file2.getAbsoluteFile());
    }
  }

  private static String readFile(File path) throws IOException {
    return new String(Files.readAllBytes(path.toPath()), StandardCharsets.UTF_8);
  }
}
