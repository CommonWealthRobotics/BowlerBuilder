/*
 * Copyright 2015 Kevin Harrington
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.neuronrobotics.bowlerstudio.util;

/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.neuronrobotics.sdk.util.IFileChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** The Class FileChangeWatcher. */
public class FileChangeWatcher {

  /** The file to watch. */
  private File fileToWatch;

  /** The run. */
  private boolean run = true;

  /** The watcher. */
  private final WatchService watcher;

  /** The keys. */
  private final Map<WatchKey, Path> keys;

  /** The recursive. */
  private final boolean recursive = false;

  /** The listeners. */
  private ArrayList<IFileChangeListener> listeners = new ArrayList<>();

  private static boolean runThread = true;

  private static HashMap<String, FileChangeWatcher> activeListener =
      new HashMap<>();
  private Thread watcherThread = null;

  /** clear the listeners */
  public static void clearAll() {
    for (final String key : activeListener.keySet()) {
      activeListener.get(key).close();
    }
    activeListener.clear();
  }

  /**
   * Start watching a file
   *
   * @param fileToWatch a file that should be watched
   * @return the watcher object for this file
   */
  public static FileChangeWatcher watch(final File fileToWatch) throws IOException {
    final String path = fileToWatch.getAbsolutePath();
    if (activeListener.get(path) == null) {
      activeListener.put(path, new FileChangeWatcher(fileToWatch));
      System.err.println("Adding file to listening " + fileToWatch.getAbsolutePath());
    }
    return activeListener.get(path);
  }

  /**
   * Instantiates a new file change watcher.
   *
   * @param fileToWatch the file to watch
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private FileChangeWatcher(final File fileToWatch) throws IOException {

    this.setFileToWatch(fileToWatch);

    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<>();
    final Path dir = Paths.get(fileToWatch.getParent());
    if (recursive) {
      System.out.format("Scanning %s ...\n", dir);
      registerAll(dir);
      System.out.println("Done.");
    } else {
      register(dir);
    }
    watcherThread =
        new Thread() {
          public void run() {
            setName("File Watcher Thread");
            // new Exception("Starting File Watcher Thread").printStackTrace();

            while (run) {
              try {
                System.err.println("Checking File: " + getFileToWatch().getAbsolutePath());
                watch();
              } catch (final Exception ex) {
                ex.printStackTrace();
              }

              try {
                Thread.sleep(100);
              } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
            // new Exception("File Watcher Thread Died").printStackTrace();
          }
        };
    watcherThread.start();
  }

  /**
   * Adds the i file change listener.
   *
   * @param l the l
   */
  public void addIFileChangeListener(final IFileChangeListener l) {
    if (!listeners.contains(l)) {
      listeners.add(l);
    }
  }

  /**
   * Removes the i file change listener.
   *
   * @param l the l
   */
  public void removeIFileChangeListener(final IFileChangeListener l) {
    listeners.remove(l);
    //        if(listeners.size()==0){
    //            close() ;
    //        }
  }

  /**
   * Cast.
   *
   * @param <T> the generic type
   * @param event the event
   * @return the watch event
   */
  @SuppressWarnings("unchecked")
  private static <T> WatchEvent<T> cast(final WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  /**
   * Register the given directory with the WatchService.
   *
   * @param dir the dir
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void register(final Path dir) throws IOException {
    final WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

    final Path prev = keys.get(key);
    if (prev == null) {
      // System.out.format("register: %s\n", dir);
    } else {
      if (!dir.equals(prev)) {
        // System.out.format("update: %s -> %s\n", prev, dir);
      }
    }

    keys.put(key, dir);
  }

  /**
   * Register the given directory, and all its sub-directories, with the WatchService.
   *
   * @param start the start
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void registerAll(final Path start) throws IOException {
    // register directory and sub-directories
    Files.walkFileTree(
        start,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
              throws IOException {
            register(dir);
            return FileVisitResult.CONTINUE;
          }
        });
  }

  /** Perfom the watch execution */
  private void watch() {

    // wait for key to be signalled
    final WatchKey key;
    try {
      key = watcher.take();
    } catch (final Exception x) {
      return;
    }
    if (!run) {
      return;
    }

    final Path dir = keys.get(key);
    if (dir == null) {
      System.err.println("WatchKey not recognized!!");
      return;
    }

    for (final WatchEvent<?> event : key.pollEvents()) {
      final WatchEvent.Kind kind = event.kind();

      // TBD - provide example of how OVERFLOW event is handled
      if (kind == OVERFLOW) {
        continue;
      }

      // Context for directory entry event is the file name of entry
      final WatchEvent<Path> ev = cast(event);
      final Path name = ev.context();
      final Path child = dir.resolve(name);
      try {
        if (!child.toFile().getCanonicalPath().equals(fileToWatch.getCanonicalPath())) {
          continue;
        }
        // print out event
        // System.out.format("%s: %s\n", event.kind().name(), child);
        System.err.println("File Changed: " + getFileToWatch().getAbsolutePath());
        for (final IFileChangeListener listener : listeners) {

          listener.onFileChange(child.toFile(), event);
          Thread.sleep(50); // pad out the events to avoid file box
          // overwrites
        }
      } catch (final Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // reset key and remove from set if directory no longer accessible
    final boolean valid = key.reset();
    if (!valid) {
      keys.remove(key);

      // all directories are inaccessible
      if (keys.isEmpty()) {
        return;
      }
    }
  }

  /**
   * Gets the file to watch.
   *
   * @return the file to watch
   */
  private File getFileToWatch() {
    return fileToWatch;
  }

  /**
   * Sets the file to watch.
   *
   * @param fileToWatch the new file to watch
   */
  private void setFileToWatch(final File fileToWatch) {
    this.fileToWatch = fileToWatch;
  }

  /**
   * Checks if is run.
   *
   * @return true, if is run
   */
  public boolean isRun() {
    return run;
  }

  /** Close. */
  private void close() {
    new Exception("File watcher closed " + fileToWatch.getAbsolutePath()).printStackTrace();
    this.run = false;
    try {
      watcher.close();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    activeListener.remove(fileToWatch.getAbsolutePath());
  }
}
