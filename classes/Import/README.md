# myLib
helpful sc classes


Import.sc works like this

let's say you are in a document where thisProcess.nowExecutingPath is not nil  
you can import modules with a relative path like so:  
if there is a file "./module.scd" in the same directory  
`'module'.import;` will add an Environment to the currentEnvironment as `~module`  
and you can access its variables like so `~module.var1;`  
if "./module" is a folder containing .scd files (file1.scd, file2.scd)  

`'module'.import;` will load them into an Environment called `~module`,  
containing each file as a submodule  
(eg `~module.file1, ~module.file2`)  
also you can import all of them individually like so:  
`'module/*'.import;`  
that will result in modules ~file1 and ~file2 being added to the currentEnvironment  


a few extra notes:  
what the import / module class is does is create a new Environment with the   
stuff defined in your module, and it sets the property 'know' to true on that
environment  
that means that the module can be used for object prototyping, and you can use
the functions as if they were the methods of an object  
for example,  
`~mod1.testfunction;`  
doesn't return the function that is contained in the keyword 'testfunction',  
it just executes it, so no need for the 'value' keyword.  
(if you actually need to return the function on its own without executing it  
you can still do `~mod1[\testfunction]`)  
the other peculiar thing about using environments for object prototyping or to  
store functions is that the environment that the module is a part of is passed  
as the function's first argument by default. This is confusing but actually  
useful, because it means that rather than just being a container for functions,  
an environment can have functions that modify the environment itself

take this function inside a module 'test.scd' for example:  
```
~testFunc2 = { arg a = 1, b = 2;
	[a, b].postln;
};
```

if you import test and try to use that function like this  
`~test.testFunc2(5, 6)`  
what will be printed to the post window is not [5,6] but actually  
[Mod(blah bah bla), 5] - the arguments get shifted up  

here's a similar version
```
~testFunc3 = { arg env, a = 1, b = 2;
	[a, b].postln;
};
```

this time, the first argument catches the environment,  
and you can use the method / function from outside as you would expect  
~test.testFunc3(3, 4) would print [3, 4]  
to get around this I've included the class ModFunc which you'll find in  
Import.sc and the alias M  
it's a wrapper that takes the function you define with normal arguments and  
returns a new function with the extra env argument at the front,  
and which executes the function you've defined within the passed environment,  
so for example 
```	
//module

~number = 3;

~testFunc1 = { arg env, a = 1, b = 2;
	env.use {
		~number * [a, b];
	}
};


~testFunc2 = { arg env, a = 1, b = 2;
	env[\number] * [a, b];
};

~testFunc3 = M { arg a = 1, b = 2;
	~number * [a, b];
};
```  
here testFunc1, testFunc2 and testFunc3 all work exactly the same  
you can put modules you want to access globally in a folder and set that folder  
as Import's default module path in your 'startup.scd' file:
Import.defaultModulePath = "/path/to/my/modules";
