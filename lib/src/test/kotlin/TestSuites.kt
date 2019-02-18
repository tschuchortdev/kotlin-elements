package com.tschuchort.kotlinelements

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
	KotlinClassElementTests::class,
	KotlinFileFacadeElementTests::class,
	KotlinObjectElementTests::class,
	KotlinInterfaceElementTests::class,
	KotlinAnnotationElementTests::class,
	KotlinAnnotationParameterElementTests::class,
	KotlinPackageElementTests::class,
	KotlinTypeAliasElementTests::class,
	KotlinConstructorElementTests::class
)
internal class KotlinElementTestSuite