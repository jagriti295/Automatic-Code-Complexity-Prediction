package com.github.mauricioaniche.ck.metric;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.github.mauricioaniche.ck.CKMethodResult;

public class MethodLevelFieldUsageCount extends ASTVisitor implements MethodLevelMetric {
	private Set<String> declaredFields;
	private Map<String, Integer> occurrences;
	private Set<String> variables;
	private boolean isFieldAccess;

	public MethodLevelFieldUsageCount() {
		declaredFields = new HashSet<>();
		this.occurrences = new HashMap<>();
		this.variables = new HashSet<>();
	}

	public boolean visit(MethodDeclaration node) {

		IMethodBinding binding = node.resolveBinding();
		if(binding==null)
			return super.visit(node);

		IVariableBinding[] fields = binding.getDeclaringClass().getDeclaredFields();

		for (IVariableBinding field : fields) {
			declaredFields.add(field.getName().toString());
		}
		return false;
	}

	public boolean visit(FieldDeclaration node) {
		return false;
	}

	public boolean visit(VariableDeclarationFragment node) {
		String var = node.getName().toString();
		variables.add(var);
		return false;
	}

	public boolean visit(FieldAccess node) {
		isFieldAccess = true;
		return super.visit(node);
	}

	public void endVisit(FieldAccess node) {
		isFieldAccess = false;
	}

	private void addField(String var) {
		if (!occurrences.containsKey(var))
			occurrences.put(var, 0);
	}

	private void plusOne(String var) {
		addField(var);
		occurrences.put(var, occurrences.get(var) + 1);
	}

	public boolean visit(SimpleName node) {

		String var = node.getIdentifier();

		if(isFieldAccess)
			addField(var);

		boolean accessFieldUsingThis = isFieldAccess && declaredFields.contains(var);
		boolean accessFieldUsingOnlyVariableName = !isFieldAccess && declaredFields.contains(var) && !variables.contains(var);

		if(accessFieldUsingThis || accessFieldUsingOnlyVariableName) {
			plusOne(var);
		}

		return super.visit(node);
	}

	@Override
	public void setResult(CKMethodResult result) {
		result.setFieldUsage(occurrences);
	}
}
