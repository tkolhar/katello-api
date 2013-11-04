package com.redhat.qe.katello.base.tngext;

import java.lang.reflect.*;
import java.util.*;
import org.testng.*;

public class TngPriorityInterceptor implements IMethodInterceptor {
	  public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
		    Comparator<IMethodInstance> comparator = new Comparator<IMethodInstance>() {
		      private int getPriority(IMethodInstance mi) {
		      int result = 0;
		      Method method = mi.getMethod().getConstructorOrMethod().getMethod();
		      TngPriority a1 = method.getAnnotation(TngPriority.class);
		      if (a1 != null) {
		        result = a1.value();
		      } else {
		        Class<?> cls = method.getDeclaringClass();
		        TngPriority classPriority = cls.getAnnotation(TngPriority.class);
		        if (classPriority != null) {
		          result = classPriority.value();
		        }
		      }
		      return result;
		    }
		 
		    public int compare(IMethodInstance m1, IMethodInstance m2) {
		      return getPriority(m1) - getPriority(m2);
		    }
		  };
		 
		  IMethodInstance[] array = methods.toArray(new IMethodInstance[methods.size()]);
		  Arrays.sort(array, comparator);
		  return Arrays.asList(array);
		}
}