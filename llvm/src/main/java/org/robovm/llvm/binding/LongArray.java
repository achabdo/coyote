/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.robovm.llvm.binding;

public class LongArray {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected LongArray(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(LongArray obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        LLVMJNI.delete_LongArray(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setValue(long value) {
    LLVMJNI.LongArray_value_set(swigCPtr, this, value);
  }

  public long getValue() {
    return LLVMJNI.LongArray_value_get(swigCPtr, this);
  }

  public LongArray(int nelements) {
    this(LLVMJNI.new_LongArray(nelements), true);
  }

  public long get(int index) {
    return LLVMJNI.LongArray_get(swigCPtr, this, index);
  }

  public void set(int index, long value) {
    LLVMJNI.LongArray_set(swigCPtr, this, index, value);
  }

}
