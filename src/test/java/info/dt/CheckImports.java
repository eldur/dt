package info.dt;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class CheckImports {

  @Test
  public void testImports() {
    String badImport = "^import com.mycila.inject.internal.*";
    File f = new File("./src");
    Iterable<File> javaFiles = getRecursivly(f, new FileFilter() {
      public boolean accept(File arg0) {
        return arg0.isDirectory() || arg0.getName().endsWith(".java");
      }
    });

    for (File java : javaFiles) {

      try {
        List<String> lines = Files.readLines(java, Charsets.UTF_8);
        for (String line : lines) {

          assertFalse(java.getAbsolutePath(), line.matches(badImport));
        }
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private Collection<File> getRecursivly(File f, FileFilter filenameFilter) {
    File[] listFiles = f.listFiles(filenameFilter);
    List<File> result = Lists.newArrayList();
    for (File fd : listFiles) {
      if (fd.isDirectory()) {
        result.addAll(getRecursivly(fd, filenameFilter));
      } else {
        result.add(fd);
      }
    }
    return result;
  }
}
