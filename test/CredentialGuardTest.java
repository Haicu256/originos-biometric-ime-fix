import com.originos.biometric.imefix.CredentialGuard;

public final class CredentialGuardTest {
    static class FakeView {
        private final Object parent;
        FakeView(Object parent) { this.parent = parent; }
        public Object getParent() { return parent; }
    }

    static class FakeAuthContainer {
        private Object mCredentialView;
        FakeAuthContainer(Object v) { this.mCredentialView = v; }
    }

    static class FakeNoFieldContainer {}

    private static void assertEq(String name, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError("FAIL " + name + ": expected=" + expected + " actual=" + actual);
        }
        System.out.println("PASS " + name);
    }

    public static void main(String[] args) {
        assertEq("null_container", false, CredentialGuard.isAlreadyAttached(null));
        assertEq("null_credential_view", false, CredentialGuard.isAlreadyAttached(new FakeAuthContainer(null)));
        assertEq("detached_credential_view", false, CredentialGuard.isAlreadyAttached(new FakeAuthContainer(new FakeView(null))));
        assertEq("attached_credential_view", true, CredentialGuard.isAlreadyAttached(new FakeAuthContainer(new FakeView(new Object()))));
        assertEq("missing_field_safe", false, CredentialGuard.isAlreadyAttached(new FakeNoFieldContainer()));
        System.out.println("ALL_TESTS_PASSED");
    }
}
