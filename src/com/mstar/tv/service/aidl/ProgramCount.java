/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: Y:\\android\\ics\\device\\mstar\\common\\app\\MLauncher\\src\\com\\mstar\\tv\\service\\aidl\\ProgramCount.aidl
 */
package com.mstar.tv.service.aidl;
public interface ProgramCount extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.mstar.tv.service.aidl.ProgramCount
{
private static final java.lang.String DESCRIPTOR = "com.mstar.tv.service.aidl.ProgramCount";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.mstar.tv.service.aidl.ProgramCount interface,
 * generating a proxy if needed.
 */
public static com.mstar.tv.service.aidl.ProgramCount asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.mstar.tv.service.aidl.ProgramCount))) {
return ((com.mstar.tv.service.aidl.ProgramCount)iin);
}
return new com.mstar.tv.service.aidl.ProgramCount.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getProgramCount:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getProgramCount();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.mstar.tv.service.aidl.ProgramCount
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int getProgramCount() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getProgramCount, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getProgramCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public int getProgramCount() throws android.os.RemoteException;
}
