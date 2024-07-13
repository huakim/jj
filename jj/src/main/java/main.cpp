#include <jni.h>
#include <iostream>
using namespace std;

#include <jni.h>

int main(int argc, char **argv)
{
    JavaVM         *vm;
    JNIEnv         *env;
    JavaVMInitArgs  vm_args;
    jint            res;
    jclass          cls;
    jmethodID       mid;
    jstring         jstr;
    jobjectArray    main_args;

    vm_args.version  = JNI_VERSION_21;

    JavaVMOption options[1];
    options[0].optionString    = "-Djava.class.path=/:./ejavac.jar";
    vm_args.options            = options;
    vm_args.nOptions           = 1;
    vm_args.ignoreUnrecognized = JNI_TRUE;

    res = JNI_CreateJavaVM(&vm, (void **)&env, &vm_args);
    if (res != JNI_OK) {
        return res;
    }

    cls = env->FindClass( "com/ejavac/AST");
    if (cls == NULL) {
        return -1;
    }

    mid = env->GetStaticMethodID( cls, "main", "([Ljava/lang/String;)V");
    if (mid == NULL) {
        return -1;
    }

    jstr      = env->NewStringUTF( "./example.java");
    main_args = env->NewObjectArray( 1, env->FindClass( "java/lang/String"), jstr);
    env->CallStaticVoidMethod(cls, mid, main_args);

    return 0;
}
