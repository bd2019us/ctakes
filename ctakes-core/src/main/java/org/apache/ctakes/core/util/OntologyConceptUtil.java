package org.apache.ctakes.core.util;

import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.log4j.Logger;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author SPF , chip-nlp
 * @version %I%
 * @since 1/8/2015
 */
final public class OntologyConceptUtil {

   static private final Logger LOGGER = Logger.getLogger( "IdentifiedAnnotationUtil" );

   static private final FeatureStructure[] EMPTY_FEATURE_ARRAY = new FeatureStructure[ 0 ];

   private OntologyConceptUtil() {
   }


   static private final Predicate<OntologyConcept> isSchemeOk
         = concept -> concept.getCodingScheme() != null && !concept.getCodingScheme().isEmpty();

   static private final Predicate<OntologyConcept> isCodeOk
         = concept -> concept.getCode() != null && !concept.getCode().isEmpty();

   static private final Function<OntologyConcept, Collection<String>> getCodeAsSet
         = concept -> new HashSet<>( Collections.singletonList( concept.getCode() ) );

   static private final BinaryOperator<Collection<String>> mergeSets
         = ( set1, set2 ) -> {
      set1.addAll( set2 );
      return set1;
   };


   /**
    * @param annotation -
    * @return array of FeatureStructure castable to array of OntologyConcept
    */
   static public FeatureStructure[] getConceptFeatureStructures( final IdentifiedAnnotation annotation ) {
      if ( annotation == null ) {
         return EMPTY_FEATURE_ARRAY;
      }
      final FSArray ontologyConcepts = annotation.getOntologyConceptArr();
      if ( ontologyConcepts == null ) {
         return EMPTY_FEATURE_ARRAY;
      }
      return ontologyConcepts.toArray();
   }

   /**
    * @param annotation -
    * @return stream of OntologyConcept
    */
   static public Stream<OntologyConcept> getOntologyConceptStream( final IdentifiedAnnotation annotation ) {
      return Arrays.stream( getConceptFeatureStructures( annotation ) )
            .filter( OntologyConcept.class::isInstance )
            .map( fs -> (OntologyConcept)fs )
            .filter( isSchemeOk )
            .filter( isCodeOk );
   }

   /**
    * @param annotation -
    * @return stream of OntologyConcept
    */
   static public Collection<OntologyConcept> getOntologyConcepts( final IdentifiedAnnotation annotation ) {
      return getOntologyConceptStream( annotation ).collect( Collectors.toSet() );
   }

   /**
    * @param annotation -
    * @return stream of all Umls Concepts associated with the annotation
    */
   static public Stream<UmlsConcept> getUmlsConceptStream( final IdentifiedAnnotation annotation ) {
      return getOntologyConceptStream( annotation )
            .filter( UmlsConcept.class::isInstance )
            .map( fs -> (UmlsConcept)fs );
   }

   /**
    * @param annotation -
    * @return set of all Umls Concepts associated with the annotation
    */
   static public Collection<UmlsConcept> getUmlsConcepts( final IdentifiedAnnotation annotation ) {
      return getUmlsConceptStream( annotation ).collect( Collectors.toSet() );
   }


   //
   //   Get cuis, tuis, or codes for a single IdentifiedAnnotation
   //

   /**
    * @param annotation -
    * @return set of all Umls cuis associated with the annotation
    */
   static public Collection<String> getCuis( final IdentifiedAnnotation annotation ) {
      return getUmlsConceptStream( annotation )
            .map( UmlsConcept::getCui )
            .collect( Collectors.toSet() );
   }

   /**
    * @param annotation -
    * @return set of all Umls tuis associated with the annotation
    */
   static public Collection<String> getTuis( final IdentifiedAnnotation annotation ) {
      return getUmlsConceptStream( annotation )
            .map( UmlsConcept::getTui )
            .collect( Collectors.toSet() );
   }

   /**
    * @param annotation -
    * @return map of ontology scheme names to a set of ontology codes associated each scheme
    */
   static public Map<String, Collection<String>> getSchemeCodes( final IdentifiedAnnotation annotation ) {
      return getOntologyConceptStream( annotation )
            .collect( Collectors.toMap( OntologyConcept::getCodingScheme, getCodeAsSet, mergeSets ) );
   }

   /**
    * @param annotation -
    * @return set of ontology codes associated with all schemes
    */
   static public Collection<String> getCodes( final IdentifiedAnnotation annotation ) {
      return getOntologyConceptStream( annotation )
            .map( OntologyConcept::getCode )
            .collect( Collectors.toSet() );
   }

   /**
    * @param annotation -
    * @param schemeName name of the scheme of interest
    * @return set of ontology codes associated the named scheme
    */
   static public Collection<String> getCodes( final IdentifiedAnnotation annotation,
                                              final String schemeName ) {
      return getOntologyConceptStream( annotation )
            .filter( concept -> schemeName.equalsIgnoreCase( concept.getCodingScheme() ) )
            .map( OntologyConcept::getCode )
            .collect( Collectors.toSet() );
   }


   //
   //   Get cuis, tuis, or codes for all IdentifiedAnnotations in a jcas
   //

   /**
    * @param jcas -
    * @return set of all cuis in jcas
    */
   static public Collection<String> getCuis( final JCas jcas ) {
      return getCuis( JCasUtil.select( jcas, IdentifiedAnnotation.class ) );
   }

   /**
    * @param jcas -
    * @return set of all tuis in jcas
    */
   static public Collection<String> getTuis( final JCas jcas ) {
      return getTuis( JCasUtil.select( jcas, IdentifiedAnnotation.class ) );
   }

   /**
    * @param jcas -
    * @return set of all ontology codes in jcas
    */
   static public Map<String, Collection<String>> getSchemeCodes( final JCas jcas ) {
      return getSchemeCodes( JCasUtil.select( jcas, IdentifiedAnnotation.class ) );
   }

   /**
    * @param jcas -
    * @return set of all ontology codes in jcas
    */
   static public Collection<String> getCodes( final JCas jcas ) {
      return getCodes( JCasUtil.select( jcas, IdentifiedAnnotation.class ) );
   }

   /**
    * @param jcas       -
    * @param schemeName name of the scheme of interest
    * @return set of ontology codes associated the named scheme
    */
   static public Collection<String> getCodes( final JCas jcas,
                                              final String schemeName ) {
      return getCodes( JCasUtil.select( jcas, IdentifiedAnnotation.class ), schemeName );
   }


   //
   //   Get cuis, tuis, or codes for all IdentifiedAnnotations in a lookup window
   //

   /**
    * @param jcas         -
    * @param lookupWindow -
    * @return set of all cuis in lookupWindow
    */
   static public <T extends Annotation> Collection<String> getCuis( final JCas jcas, final T lookupWindow ) {
      return getCuis( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ) );
   }

   /**
    * @param jcas         -
    * @param lookupWindow -
    * @return set of all tuis in lookupWindow
    */
   static public <T extends Annotation> Collection<String> getTuis( final JCas jcas, final T lookupWindow ) {
      return getTuis( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ) );
   }

   /**
    * @param jcas         -
    * @param lookupWindow -
    * @return map of all schemes and their codes in lookupWindow
    */
   static public <T extends Annotation> Map<String, Collection<String>> getSchemeCodes( final JCas jcas,
                                                                                        final T lookupWindow ) {
      return getSchemeCodes( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ) );
   }

   /**
    * @param jcas         -
    * @param lookupWindow -
    * @return set of all codes in lookupWindow
    */
   static public <T extends Annotation> Collection<String> getCodes( final JCas jcas, final T lookupWindow ) {
      return getCodes( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ) );
   }

   /**
    * @param jcas         -
    * @param lookupWindow -
    * @param schemeName   name of the scheme of interest
    * @return set of ontology codes associated the named scheme
    */
   static public <T extends Annotation> Collection<String> getCodes( final JCas jcas, final T lookupWindow,
                                                                     final String schemeName ) {
      return getCodes( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ), schemeName );
   }


   //
   //   Get cuis, tuis, or codes for a collections of IdentifiedAnnotations
   //

   /**
    * @param annotations -
    * @return set of all Umls cuis associated with the annotations
    */
   static public Collection<String> getCuis( final Collection<IdentifiedAnnotation> annotations ) {
      return annotations.stream()
            .map( OntologyConceptUtil::getCuis )
            .flatMap( Collection::stream )
            .collect( Collectors.toSet() );
   }

   /**
    * @param annotations -
    * @return set of all Umls tuis associated with the annotation
    */
   static public Collection<String> getTuis( final Collection<IdentifiedAnnotation> annotations ) {
      return annotations.stream()
            .map( OntologyConceptUtil::getTuis )
            .flatMap( Collection::stream )
            .collect( Collectors.toSet() );

   }

   /**
    * @param annotations -
    * @return map of ontology scheme names to a set of ontology codes associated each scheme
    */
   static public Map<String, Collection<String>> getSchemeCodes( final Collection<IdentifiedAnnotation> annotations ) {
      return annotations.stream()
            .map( OntologyConceptUtil::getSchemeCodes )
            .map( Map::entrySet )
            .flatMap( Collection::stream )
            .collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue, mergeSets ) );
   }

   /**
    * @param annotations -
    * @return set of ontology codes associated with all schemes
    */
   static public Collection<String> getCodes( final Collection<IdentifiedAnnotation> annotations ) {
      return annotations.stream()
            .map( OntologyConceptUtil::getCodes )
            .flatMap( Collection::stream )
            .collect( Collectors.toSet() );
   }

   /**
    * @param annotations -
    * @param schemeName  name of the scheme of interest
    * @return set of ontology codes associated the named scheme
    */
   static public Collection<String> getCodes( final Collection<IdentifiedAnnotation> annotations,
                                              final String schemeName ) {
      return annotations.stream()
            .map( annotation -> getCodes( annotation, schemeName ) )
            .flatMap( Collection::stream )
            .collect( Collectors.toSet() );
   }


   //
   //   Get all IdentifiedAnnotations in jcas with given cui, tui, or code
   //

   /**
    * @param jcas -
    * @param cui  cui of interest
    * @return all IdentifiedAnnotations that have the given cui
    */
   static public Collection<IdentifiedAnnotation> getAnnotationsByCui( final JCas jcas,
                                                                       final String cui ) {
      return getAnnotationsByCui( JCasUtil.select( jcas, IdentifiedAnnotation.class ), cui );
   }

   /**
    * @param jcas -
    * @param tui  tui of interest
    * @return all IdentifiedAnnotations that have the given tui
    */
   static public Collection<IdentifiedAnnotation> getAnnotationsByTui( final JCas jcas,
                                                                       final String tui ) {
      return getAnnotationsByTui( JCasUtil.select( jcas, IdentifiedAnnotation.class ), tui );
   }

   /**
    * @param jcas -
    * @param code code of interest
    * @return all IdentifiedAnnotations that have the given code
    */
   static public Collection<IdentifiedAnnotation> getAnnotationsByCode( final JCas jcas,
                                                                        final String code ) {
      return getAnnotationsByCode( JCasUtil.select( jcas, IdentifiedAnnotation.class ), code );
   }


   //
   //   Get all IdentifiedAnnotations in lookup window with given cui, tui, or code
   //

   /**
    * @param jcas         -
    * @param lookupWindow
    * @param cui          cui of interest
    * @return all IdentifiedAnnotations that have the given cui
    */
   static public <T extends Annotation> Collection<IdentifiedAnnotation> getAnnotationsByCui( final JCas jcas,
                                                                                              final T lookupWindow,
                                                                                              final String cui ) {
      return getAnnotationsByCui( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ), cui );
   }

   /**
    * @param jcas         -
    * @param lookupWindow
    * @param tui          tui of interest
    * @return all IdentifiedAnnotations that have the given tui
    */
   static public <T extends Annotation> Collection<IdentifiedAnnotation> getAnnotationsByTui( final JCas jcas,
                                                                                              final T lookupWindow,
                                                                                              final String tui ) {
      return getAnnotationsByTui( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ), tui );
   }

   /**
    * @param jcas         -
    * @param lookupWindow
    * @param code         code of interest
    * @return all IdentifiedAnnotations that have the given code
    */
   static public <T extends Annotation> Collection<IdentifiedAnnotation> getAnnotationsByCode( final JCas jcas,
                                                                                               final T lookupWindow,
                                                                                               final String code ) {
      return getAnnotationsByCode( JCasUtil.selectCovered( jcas, IdentifiedAnnotation.class, lookupWindow ), code );
   }


   //
   //   Get all IdentifiedAnnotations in a collection of annotations with given cui, tui, or code
   //

   /**
    * @param annotations annotations for which codes should be found
    * @param cui         cui of interest
    * @return all IdentifiedAnnotations that have the given cui
    */
   static public Collection<IdentifiedAnnotation> getAnnotationsByCui(
         final Collection<IdentifiedAnnotation> annotations,
         final String cui ) {
      return annotations.stream()
            .filter( annotation -> getCuis( annotation ).contains( cui ) )
            .collect( Collectors.toSet() );
   }


   /**
    * @param annotations annotations for which codes should be found
    * @param tui         tui of interest
    * @return all IdentifiedAnnotations that have the given tui
    */
   static public Collection<IdentifiedAnnotation> getAnnotationsByTui(
         final Collection<IdentifiedAnnotation> annotations,
         final String tui ) {
      return annotations.stream()
            .filter( annotation -> getTuis( annotation ).contains( tui ) )
            .collect( Collectors.toSet() );
   }


   /**
    * @param annotations annotations for which codes should be found
    * @param code        code of interest
    * @return all IdentifiedAnnotations that have the given code
    */
   static public Collection<IdentifiedAnnotation> getAnnotationsByCode(
         final Collection<IdentifiedAnnotation> annotations,
         final String code ) {
      return annotations.stream()
            .filter( annotation -> getCodes( annotation ).contains( code ) )
            .collect( Collectors.toSet() );
   }

}
