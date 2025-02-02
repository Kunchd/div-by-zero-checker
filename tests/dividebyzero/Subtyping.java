import org.checkerframework.checker.dividebyzero.qual.*;

// Test subtyping relationships for the Divide By Zero Checker.
// The file contains "// ::" comments to indicate expected errors and warnings.

@SuppressWarnings(
    "anno.on.irrelevant"
)
class SubtypeTest {
  /// You will want a test like this for every pair of qualifiers in your type hierarchy.
   void oneSubtypingRelationship(@Top int x, @Bottom int y) {
     @Top int a = x;
     @Top int b = y;
     // :: error: assignment
     @Bottom int c = x; // expected error on this line, as indicated just above
     @Bottom int d = y;
   }
}
